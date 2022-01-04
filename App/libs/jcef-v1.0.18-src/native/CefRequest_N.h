/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_cef_network_CefRequest_N */

#ifndef _Included_org_cef_network_CefRequest_N
#define _Included_org_cef_network_CefRequest_N
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_Create
 * Signature: ()Lorg/cef/network/CefRequest_N;
 */
JNIEXPORT jobject JNICALL Java_org_cef_network_CefRequest_1N_N_1Create(JNIEnv*,
                                                                       jclass);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_Dispose
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_cef_network_CefRequest_1N_N_1Dispose(JNIEnv*,
                                                                     jobject,
                                                                     jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetIdentifier
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetIdentifier(JNIEnv*, jobject, jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_IsReadOnly
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_cef_network_CefRequest_1N_N_1IsReadOnly(JNIEnv*, jobject, jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetURL
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_cef_network_CefRequest_1N_N_1GetURL(JNIEnv*,
                                                                       jobject,
                                                                       jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_SetURL
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_cef_network_CefRequest_1N_N_1SetURL(JNIEnv*,
                                                                    jobject,
                                                                    jlong,
                                                                    jstring);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetMethod
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetMethod(JNIEnv*, jobject, jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_SetMethod
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_cef_network_CefRequest_1N_N_1SetMethod(JNIEnv*,
                                                                       jobject,
                                                                       jlong,
                                                                       jstring);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_SetReferrer
 * Signature: (JLjava/lang/String;Lorg/cef/network/CefRequest/ReferrerPolicy;)V
 */
JNIEXPORT void JNICALL
Java_org_cef_network_CefRequest_1N_N_1SetReferrer(JNIEnv*,
                                                  jobject,
                                                  jlong,
                                                  jstring,
                                                  jobject);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetReferrerURL
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetReferrerURL(JNIEnv*, jobject, jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetReferrerPolicy
 * Signature: (J)Lorg/cef/network/CefRequest/ReferrerPolicy;
 */
JNIEXPORT jobject JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetReferrerPolicy(JNIEnv*,
                                                        jobject,
                                                        jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetPostData
 * Signature: (J)Lorg/cef/network/CefPostData;
 */
JNIEXPORT jobject JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetPostData(JNIEnv*, jobject, jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_SetPostData
 * Signature: (JLorg/cef/network/CefPostData;)V
 */
JNIEXPORT void JNICALL
Java_org_cef_network_CefRequest_1N_N_1SetPostData(JNIEnv*,
                                                  jobject,
                                                  jlong,
                                                  jobject);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetHeaderByName
 * Signature: (JLjava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetHeaderByName(JNIEnv*,
                                                      jobject,
                                                      jlong,
                                                      jstring);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_SetHeaderByName
 * Signature: (JLjava/lang/String;Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL
Java_org_cef_network_CefRequest_1N_N_1SetHeaderByName(JNIEnv*,
                                                      jobject,
                                                      jlong,
                                                      jstring,
                                                      jstring,
                                                      jboolean);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetHeaderMap
 * Signature: (JLjava/util/Map;)V
 */
JNIEXPORT void JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetHeaderMap(JNIEnv*,
                                                   jobject,
                                                   jlong,
                                                   jobject);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_SetHeaderMap
 * Signature: (JLjava/util/Map;)V
 */
JNIEXPORT void JNICALL
Java_org_cef_network_CefRequest_1N_N_1SetHeaderMap(JNIEnv*,
                                                   jobject,
                                                   jlong,
                                                   jobject);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_Set
 * Signature:
 * (JLjava/lang/String;Ljava/lang/String;Lorg/cef/network/CefPostData;Ljava/util/Map;)V
 */
JNIEXPORT void JNICALL Java_org_cef_network_CefRequest_1N_N_1Set(JNIEnv*,
                                                                 jobject,
                                                                 jlong,
                                                                 jstring,
                                                                 jstring,
                                                                 jobject,
                                                                 jobject);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetFlags
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_cef_network_CefRequest_1N_N_1GetFlags(JNIEnv*,
                                                                      jobject,
                                                                      jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_SetFlags
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_org_cef_network_CefRequest_1N_N_1SetFlags(JNIEnv*,
                                                                      jobject,
                                                                      jlong,
                                                                      jint);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetFirstPartyForCookies
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetFirstPartyForCookies(JNIEnv*,
                                                              jobject,
                                                              jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_SetFirstPartyForCookies
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_cef_network_CefRequest_1N_N_1SetFirstPartyForCookies(JNIEnv*,
                                                              jobject,
                                                              jlong,
                                                              jstring);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetResourceType
 * Signature: (J)Lorg/cef/network/CefRequest/ResourceType;
 */
JNIEXPORT jobject JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetResourceType(JNIEnv*, jobject, jlong);

/*
 * Class:     org_cef_network_CefRequest_N
 * Method:    N_GetTransitionType
 * Signature: (J)Lorg/cef/network/CefRequest/TransitionType;
 */
JNIEXPORT jobject JNICALL
Java_org_cef_network_CefRequest_1N_N_1GetTransitionType(JNIEnv*,
                                                        jobject,
                                                        jlong);

#ifdef __cplusplus
}
#endif
#endif
