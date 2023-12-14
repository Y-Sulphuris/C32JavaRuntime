/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class c32_memory_Memory */

#ifndef _Included_c32_memory_Memory
#define _Included_c32_memory_Memory
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     c32_memory_Memory
 * Method:    malloc
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_malloc
  (JNIEnv *, jclass, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    calloc
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_calloc
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    free
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_free
  (JNIEnv *, jclass, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    realloc
 * Signature: (JJ)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_realloc
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    getByte0
 * Signature: (J)B
 */
JNIEXPORT jbyte JNICALL Java_c32_memory_Memory_getByte0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putByte0
 * Signature: (JB)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putByte0
  (JNIEnv *, jclass, jlong, jbyte);

/*
 * Class:     c32_memory_Memory
 * Method:    getShort0
 * Signature: (J)S
 */
JNIEXPORT jshort JNICALL Java_c32_memory_Memory_getShort0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putShort0
 * Signature: (JS)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putShort0
  (JNIEnv *, jclass, jlong, jshort);

/*
 * Class:     c32_memory_Memory
 * Method:    getInt0
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_c32_memory_Memory_getInt0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putInt0
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putInt0
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     c32_memory_Memory
 * Method:    getLong0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_getLong0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putLong0
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putLong0
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    news
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_news
  (JNIEnv *, jclass, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    delete
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_delete
  (JNIEnv *, jclass, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    objectFieldOffset0
 * Signature: (Ljava/lang/reflect/Field;)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_objectFieldOffset0
  (JNIEnv *, jclass, jobject);

/*
 * Class:     c32_memory_Memory
 * Method:    getRefByte0
 * Signature: (Ljava/lang/Object;J)B
 */
JNIEXPORT jbyte JNICALL Java_c32_memory_Memory_getRefByte0
  (JNIEnv *, jclass, jobject, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putRefByte0
 * Signature: (Ljava/lang/Object;JB)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putRefByte0
  (JNIEnv *, jclass, jobject, jlong, jbyte);

/*
 * Class:     c32_memory_Memory
 * Method:    getRefBoolean0
 * Signature: (Ljava/lang/Object;J)Z
 */
JNIEXPORT jboolean JNICALL Java_c32_memory_Memory_getRefBoolean0
  (JNIEnv *, jclass, jobject, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putRefBoolean0
 * Signature: (Ljava/lang/Object;JZ)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putRefBoolean0
  (JNIEnv *, jclass, jobject, jlong, jboolean);

/*
 * Class:     c32_memory_Memory
 * Method:    getRefShort0
 * Signature: (Ljava/lang/Object;J)S
 */
JNIEXPORT jshort JNICALL Java_c32_memory_Memory_getRefShort0
  (JNIEnv *, jclass, jobject, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putRefShort0
 * Signature: (Ljava/lang/Object;JS)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putRefShort0
  (JNIEnv *, jclass, jobject, jlong, jshort);

/*
 * Class:     c32_memory_Memory
 * Method:    getRefChar0
 * Signature: (Ljava/lang/Object;J)C
 */
JNIEXPORT jchar JNICALL Java_c32_memory_Memory_getRefChar0
  (JNIEnv *, jclass, jobject, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putRefChar0
 * Signature: (Ljava/lang/Object;JC)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putRefChar0
  (JNIEnv *, jclass, jobject, jlong, jchar);

/*
 * Class:     c32_memory_Memory
 * Method:    getRefInt0
 * Signature: (Ljava/lang/Object;J)I
 */
JNIEXPORT jint JNICALL Java_c32_memory_Memory_getRefInt0
  (JNIEnv *, jclass, jobject, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putRefInt0
 * Signature: (Ljava/lang/Object;JI)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putRefInt0
  (JNIEnv *, jclass, jobject, jlong, jint);

/*
 * Class:     c32_memory_Memory
 * Method:    getRefFloat0
 * Signature: (Ljava/lang/Object;J)F
 */
JNIEXPORT jfloat JNICALL Java_c32_memory_Memory_getRefFloat0
  (JNIEnv *, jclass, jobject, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putRefFloat0
 * Signature: (Ljava/lang/Object;JF)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putRefFloat0
  (JNIEnv *, jclass, jobject, jlong, jfloat);

/*
 * Class:     c32_memory_Memory
 * Method:    getRefLong0
 * Signature: (Ljava/lang/Object;J)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_getRefLong0
  (JNIEnv *, jclass, jobject, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putRefLong0
 * Signature: (Ljava/lang/Object;JJ)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putRefLong0
  (JNIEnv *, jclass, jobject, jlong, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    getRefDouble0
 * Signature: (Ljava/lang/Object;J)D
 */
JNIEXPORT jdouble JNICALL Java_c32_memory_Memory_getRefDouble0
  (JNIEnv *, jclass, jobject, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putRefDouble0
 * Signature: (Ljava/lang/Object;JD)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putRefDouble0
  (JNIEnv *, jclass, jobject, jlong, jdouble);

/*
 * Class:     c32_memory_Memory
 * Method:    getRefObject0
 * Signature: (Ljava/lang/Object;J)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_c32_memory_Memory_getRefObject0
  (JNIEnv *, jclass, jobject, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putRefObject0
 * Signature: (Ljava/lang/Object;JLjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putRefObject0
  (JNIEnv *, jclass, jobject, jlong, jobject);

/*
 * Class:     c32_memory_Memory
 * Method:    memmove
 * Signature: (JJJ)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_memmove
  (JNIEnv *, jclass, jlong, jlong, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    memccpy
 * Signature: (JJBJ)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_memccpy
  (JNIEnv *, jclass, jlong, jlong, jbyte, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    memcmp
 * Signature: (JJJ)I
 */
JNIEXPORT jint JNICALL Java_c32_memory_Memory_memcmp
  (JNIEnv *, jclass, jlong, jlong, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    copyMemory0
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_copyMemory0
  (JNIEnv *, jclass, jlong, jlong, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    copyMemoryRef0
 * Signature: (Ljava/lang/Object;JLjava/lang/Object;JJ[I)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_copyMemoryRef0
  (JNIEnv *, jclass, jobject, jlong, jobject, jlong, jlong, jintArray);

/*
 * Class:     c32_memory_Memory
 * Method:    setMemory0
 * Signature: (JJB)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_setMemory0
  (JNIEnv *, jclass, jlong, jlong, jbyte);

/*
 * Class:     c32_memory_Memory
 * Method:    setRefMemory0
 * Signature: (Ljava/lang/Object;JJB[I)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_setRefMemory0
  (JNIEnv *, jclass, jobject, jlong, jlong, jbyte, jintArray);

/*
 * Class:     c32_memory_Memory
 * Method:    pageSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_c32_memory_Memory_pageSize
  (JNIEnv *, jclass);

/*
 * Class:     c32_memory_Memory
 * Method:    addressSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_c32_memory_Memory_addressSize
  (JNIEnv *, jclass);

/*
 * Class:     c32_memory_Memory
 * Method:    getAddress0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_c32_memory_Memory_getAddress0
  (JNIEnv *, jclass, jlong);

/*
 * Class:     c32_memory_Memory
 * Method:    putAddress0
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_c32_memory_Memory_putAddress0
  (JNIEnv *, jclass, jlong, jlong);

#ifdef __cplusplus
}
#endif
#endif
