#!/usr/bin/env rdmd
/** 
 * A simple script that generates the keys needed by DigiOPP Auth for signing
 * tokens and other cryptographic needs. Use this in your development
 * environment to ensure that your local auth project can issue keys to other
 * development services.
 * 
 * Authors: Andrew Lalis
 */
module gen_keys;

import std.stdio;
import std.file;

const privateKeyFile = "private_key.pem";
const privateKeyDerFile = "private_key.der";
const publicKeyDerFile = "public_key.der";

const cmdGenRSAPrivateKey = "openssl genrsa -out private_key.pem 2048";
const cmdGenDERPrivateKey = "openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.der -nocrypt";
const cmdGenDERPublicKey = "openssl rsa -in private_key.pem -pubout -outform DER -out public_key.der";

void removeIfExists(string[] files...) {
    foreach (f; files) if (exists(f)) remove(f);
}

void genKeys(string[] files...) {
    import std.process : executeShell;
    import std.algorithm : canFind;
    if (canFind(files, privateKeyFile)) executeShell(cmdGenRSAPrivateKey);
    if (canFind(files, privateKeyDerFile)) executeShell(cmdGenDERPrivateKey);
    if (canFind(files, publicKeyDerFile)) executeShell(cmdGenDERPublicKey);
}

void main() {
    if (!exists(privateKeyFile)) {
        writeln("No RSA private key found. Regenerating all key files.");
        removeIfExists(privateKeyDerFile, publicKeyDerFile);
        genKeys(privateKeyFile, privateKeyDerFile, publicKeyDerFile);
    } else if (!exists(privateKeyDerFile)) {
        writeln("No DER private key found. Regenerating private and public DER files.");
        removeIfExists(privateKeyDerFile, publicKeyDerFile);
        genKeys(privateKeyDerFile, publicKeyDerFile);
    } else if (!exists(publicKeyDerFile)) {
        writeln("No DER public key found. Regenerating it.");
        genKeys(publicKeyDerFile);
    }
}
