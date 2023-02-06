package nl.andrewlalis.gymboard_api.domains.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "auth_user_preferences")
public class UserPreferences {
	@Id
	@Column(name = "user_id", length = 26)
	private String userId;

	@OneToOne(optional = false, fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	/**
	 * Flag which, if true, indicates that the user's account is private, and
	 * only approved users may view certain information about them.
	 */
	@Column(nullable = false)
	private boolean accountPrivate = false;

	/**
	 * The user's preferred locale. This should always refer to one of the
	 * available locales offered by the front-end app.
	 */
	@Column(nullable = false)
	private String locale = "en-US";

	public UserPreferences() {}

	public UserPreferences(User user) {
		this.user = user;
		this.userId = user.getId();
	}

	public String getUserId() {
		return userId;
	}

	public User getUser() {
		return user;
	}

	public boolean isAccountPrivate() {
		return accountPrivate;
	}

	public void setAccountPrivate(boolean accountPrivate) {
		this.accountPrivate = accountPrivate;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
}
