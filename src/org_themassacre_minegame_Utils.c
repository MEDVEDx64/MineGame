#include "org_themassacre_minegame_Utils.h"

#include <jni.h>
#include <openssl/sha.h>

JNIEXPORT jbyteArray JNICALL Java_org_themassacre_minegame_Utils_computeHashNative (JNIEnv *env, jclass thisObj, jbyteArray data) {
    jboolean isCopy;
    jbyte* b = (*env)->GetByteArrayElements(env, data, &isCopy);
    jsize len = (*env)->GetArrayLength(env, data);
    
    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, (unsigned char*)b, len);
    SHA256_Final(hash, &sha256);
    
    (*env)->ReleaseByteArrayElements(env, data, b, 0);
    jbyteArray out = (*env)->NewByteArray(env, SHA256_DIGEST_LENGTH);
    (*env)->SetByteArrayRegion(env, out, 0, SHA256_DIGEST_LENGTH, hash);
    
    return out;
}