import {defaultPaginationOptions, Page, PaginationOptions} from 'src/api/main/models';
import {isScrolledToBottom} from 'src/utils';
import {Ref} from 'vue';

export type PageFetcherFunction<ElementType> = (paginationOptions: PaginationOptions) => Promise<Page<ElementType> | undefined>;

/**
 * A class that manages an "infinite loading" list of elements from a paginated
 * API endpoint. The loader will use a supplied function to load more pages
 * when the user reaches the bottom of the page or if pagination options are
 * updated.
 *
 * This loader should be given a reference to an array of elements that can be
 * updated, to update a reactive Vue display.
 */
export default class InfinitePageLoader<ElementType> {
  private paginationOptions: PaginationOptions = defaultPaginationOptions();
  private latestPage: Page<ElementType> | null = null;
  private readonly elements: Ref<ElementType[]>;
  private readonly pageFetcher: PageFetcherFunction<ElementType>;
  private fetching = false; // Internal flag used to make sure only one fetch operation happens at a time.

  /**
   * Constructs the loader with a reference to a list of elements to manage,
   * and a fetcher function for fetching pages.
   * @param elements A reference to a reactive list of elements to manage.
   * @param pageFetcher A function for fetching pages of elements.
   */
  public constructor(elements: Ref<ElementType[]>, pageFetcher: PageFetcherFunction<ElementType>) {
    this.elements = elements;
    this.pageFetcher = pageFetcher;
  }

  /**
   * Sets the pagination options for this loader. Doing so will reset the
   * loader's state and means that it'll reload elements from the start.
   * @param opts The pagination options to apply.
   */
  public async setPagination(opts: PaginationOptions) {
    this.paginationOptions = opts;
    this.elements.value.length = 0;
    while (this.shouldFetchNextPage()) {
      await this.loadNextPage();
    }
  }

  /**
   * Registers a window scroll listener that will try to fetch more elements
   * when the user has scrolled to the bottom of the page.
   */
  public registerWindowScrollListener() {
    window.addEventListener('scroll', async () => {
      if (this.shouldFetchNextPage()) {
        await this.loadNextPage();
      }
    });
  }

  private shouldFetchNextPage(): boolean {
    return !this.fetching && isScrolledToBottom(10) && (!this.latestPage || !this.latestPage.last);
  }

  private async loadNextPage() {
    this.fetching = true;
    const page = await this.pageFetcher(this.paginationOptions);
    if (page) {
      this.latestPage = page;
      this.elements.value.push(...this.latestPage.content);
      this.paginationOptions.page++;
    }
    this.fetching = false;
  }
};
