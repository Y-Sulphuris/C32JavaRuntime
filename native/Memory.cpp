//
// Created by lidia on 13.12.2023.
//
#include <cstdlib>
#include <cstdint>
#include <cstring>
#include "c32_extern_Memory.h"

extern "C" {

#define bitcast(T) *(T*)&

#define Memory(T) Java_c32_extern_Memory_##T
#define MemoryCritical(T) JavaCritical_c32_extern_Memory_##T


JNIEXPORT jlong JNICALL Memory(malloc)(JNIEnv *, jclass, jlong Size) {
	return (jlong)malloc(Size);
}

JNIEXPORT jlong JNICALL Memory(calloc)(JNIEnv *, jclass, jlong NumOfElements, jlong SizeOfElements) {
	return (jlong)calloc(NumOfElements,SizeOfElements);
}

JNIEXPORT void JNICALL Memory(free)(JNIEnv *, jclass, jlong ptr) {
	free((void*)ptr);
}

JNIEXPORT void JNICALL MemoryCritical(free)(jlong ptr) {
	free((void*)ptr);
}


const uint32_t ADD_SHIFT = 2;

//	byte
//get
JNIEXPORT jbyte JNICALL Memory(getByte0)(JNIEnv *, jclass, jlong ptr) {
	return *(jbyte*)ptr;
}
JNIEXPORT jbyte JNICALL MemoryCritical(getByte0)(jlong ptr) {
	return *(jbyte*)ptr;
}
//set
JNIEXPORT void JNICALL Memory(putByte0)(JNIEnv *, jclass, jlong ptr, jbyte x) {
	*(jbyte*)ptr = x;
}
JNIEXPORT void JNICALL MemoryCritical(putByte0)(jlong ptr, jbyte x) {
	*(jbyte*)ptr = x;
}

//ref
//get
JNIEXPORT jbyte JNICALL Memory(getRefByte0)(JNIEnv* env, jclass, jobject ref, jlong offset) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	return env->GetByteField(ref,id);
}
//set
JNIEXPORT void JNICALL Memory(putRefByte0)(JNIEnv* env, jclass, jobject ref, jlong offset, jbyte val) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	env->SetByteField(ref,id,val);
}


//	short
//get
JNIEXPORT jshort JNICALL Memory(getShort0)(JNIEnv *, jclass, jlong ptr) {
	return *(jshort*)ptr;
}
JNIEXPORT jshort JNICALL MemoryCritical(getShort0)(jlong ptr) {
	return *(jshort*)ptr;
}
//set
JNIEXPORT void JNICALL Memory(putShort0)(JNIEnv *, jclass, jlong ptr, jshort x) {
	*(jshort*)ptr = x;
}
JNIEXPORT void JNICALL MemoryCritical(putShort0)(jlong ptr, jshort x) {
	*(jshort*)ptr = x;
}

//ref
//get
JNIEXPORT jshort JNICALL Memory(getRefShort0)(JNIEnv* env, jclass, jobject ref, jlong offset) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	return env->GetShortField(ref,id);
}
//set
JNIEXPORT void JNICALL Memory(putRefShort0)(JNIEnv* env, jclass, jobject ref, jlong offset, jshort val) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	env->SetShortField(ref,id,val);
}

//	int
//get
JNIEXPORT jint JNICALL Memory(getInt0)(JNIEnv *, jclass, jlong ptr) {
	return *(jint*)ptr;
}
JNIEXPORT jint JNICALL MemoryCritical(getInt0)(jlong ptr) {
	return *(jint*)ptr;
}
//set
JNIEXPORT void JNICALL Memory(putInt0)(JNIEnv *, jclass, jlong ptr, jint x) {
	*(jint*)ptr = x;
}
JNIEXPORT void JNICALL MemoryCritical(putInt0)(jlong ptr, jint x) {
	*(jint*)ptr = x;
}

//ref
//get
JNIEXPORT jint JNICALL Memory(getRefInt0)(JNIEnv* env, jclass, jobject ref, jlong offset) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	return env->GetIntField(ref,id);
}
//set
JNIEXPORT void JNICALL Memory(putRefInt0)(JNIEnv* env, jclass, jobject ref, jlong offset, jint val) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	env->SetIntField(ref,id,val);
}


//	long
//get
JNIEXPORT jlong JNICALL Memory(getLong0)(JNIEnv *, jclass, jlong ptr) {
	return *(jlong*)ptr;
}
JNIEXPORT jlong JNICALL MemoryCritical(getLong0)(jlong ptr) {
	return *(jlong*)ptr;
}

//set
JNIEXPORT void JNICALL Memory(putLong0)(JNIEnv *, jclass, jlong ptr, jlong x) {
	*(jlong*)ptr = x;
}
JNIEXPORT void JNICALL MemoryCritical(putLong0)(jlong ptr, jlong x) {
*(jlong*)ptr = x;
}

//ref
//get
JNIEXPORT jlong JNICALL Memory(getRefLong0)(JNIEnv* env, jclass, jobject ref, jlong offset) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	return env->GetLongField(ref,id);
}
//set
JNIEXPORT void JNICALL Memory(putRefLong0)(JNIEnv* env, jclass, jobject ref, jlong offset, jlong val) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	env->SetLongField(ref,id,val);
}




JNIEXPORT jlong JNICALL Memory(memmove)(JNIEnv *, jclass, jlong Dst, jlong Src, jlong Size) {
	return reinterpret_cast<jlong>(memmove(reinterpret_cast<void *>(Dst), reinterpret_cast<const void *>(Src), Size));
}
JNIEXPORT jlong JNICALL MemoryCritical(memmove)(jlong Dst, jlong Src, jlong Size) {
	return reinterpret_cast<jlong>(memmove(reinterpret_cast<void *>(Dst), reinterpret_cast<const void *>(Src), Size));
}


JNIEXPORT jlong JNICALL Memory(memccpy)(JNIEnv *, jclass, jlong Dst, jlong Src, jbyte _Val, jlong Size) {
	return reinterpret_cast<jlong>(_memccpy(reinterpret_cast<void *>(Dst), reinterpret_cast<const void *>(Src), _Val, Size));
}
JNIEXPORT jlong JNICALL MemoryCritical(memccpy)(jlong Dst, jlong Src, jbyte _Val, jlong Size) {
	return reinterpret_cast<jlong>(_memccpy(reinterpret_cast<void *>(Dst), reinterpret_cast<const void *>(Src), _Val, Size));
}


JNIEXPORT jint JNICALL Memory(memcmp)(JNIEnv *, jclass, jlong Buf1, jlong Buf2, jlong Size) {
	return memcmp(reinterpret_cast<const void *>(Buf1), reinterpret_cast<const void *>(Buf2), Size);
}
JNIEXPORT jint JNICALL MemoryCritical(memcmp)(jlong Buf1, jlong Buf2, jlong Size) {
	return memcmp(reinterpret_cast<const void *>(Buf1), reinterpret_cast<const void *>(Buf2), Size);
}

JNIEXPORT void JNICALL Memory(copyMemory0)(JNIEnv *, jclass, jlong Dst, jlong Src, jlong Size) {
	memcpy(reinterpret_cast<void *>(Dst), reinterpret_cast<const void *>(Src), Size);
}
JNIEXPORT void JNICALL MemoryCritical(copyMemory0)(jlong Dst, jlong Src, jlong Size) {
	memcpy(reinterpret_cast<void *>(Dst), reinterpret_cast<const void *>(Src), Size);
}


JNIEXPORT void JNICALL Memory(copyMemoryRef0)(JNIEnv *env, jclass, jobject dstbase, jlong _Dst, jobject srcbase, jlong _Src, jlong _Size, jintArray lock) {
	static jboolean isCopy = false;
	void* locked = env->GetPrimitiveArrayCritical(lock,&isCopy);
	memcpy(bitcast(char*)*dstbase + _Dst, bitcast(char*)*srcbase + _Src, _Size);
	env->ReleasePrimitiveArrayCritical(lock,locked,JNI_ABORT);
}
JNIEXPORT void JNICALL MemoryCritical(copyMemoryRef0)(jobject dstbase, jlong _Dst, jobject srcbase, jlong _Src, jlong _Size, jint*, jint) {
	memcpy(bitcast(char*)*dstbase + _Dst, bitcast(char*)*srcbase + _Src, _Size);
}



JNIEXPORT void JNICALL Memory(setMemory0)(JNIEnv *, jclass, jlong _Dst, jlong _Size, jbyte _Val) {
	memset(reinterpret_cast<void *>(_Dst), _Val, _Size);
}
JNIEXPORT void JNICALL MemoryCritical(setMemory0)(jlong _Dst, jlong _Size, jbyte _Val) {
	memset(reinterpret_cast<void *>(_Dst), _Val, _Size);
}

JNIEXPORT void JNICALL Memory(setRefMemory0)(JNIEnv* env, jclass, jobject base, jlong _Dst, jlong _Size, jbyte _Val, jintArray lock) {
	static jboolean isCopy = false;
	void* locked = env->GetPrimitiveArrayCritical(lock,&isCopy);
	memset(bitcast(char*)*base + _Dst, _Val, _Size);
	env->ReleasePrimitiveArrayCritical(lock,locked,JNI_ABORT);
}
JNIEXPORT void JNICALL MemoryCritical(setRefMemory0)(jobject base, jlong _Dst, jlong _Size, jbyte _Val, jint*, jint) {
	memset(bitcast(char*)*base + _Dst, _Val, _Size);//doesn't need lock
}



JNIEXPORT jlong JNICALL Memory(objectFieldOffset0)(JNIEnv* env, jclass, jobject field) {
	jfieldID id = env->FromReflectedField(field);
	uintptr_t result = (uintptr_t) id >> ADD_SHIFT;
	return result;
}


JNIEXPORT jboolean JNICALL Memory(getRefBoolean0)(JNIEnv* env, jclass, jobject ref, jlong offset) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	return env->GetBooleanField(ref,id);
}

JNIEXPORT void JNICALL Memory(putRefBoolean0)(JNIEnv* env, jclass, jobject ref, jlong offset, jboolean val) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	env->SetBooleanField(ref,id,val);
}



JNIEXPORT jchar JNICALL Memory(getRefChar0)(JNIEnv* env, jclass, jobject ref, jlong offset) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	return env->GetCharField(ref,id);
}

JNIEXPORT void JNICALL Memory(putRefChar0)(JNIEnv* env, jclass, jobject ref, jlong offset, jchar val) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	env->SetCharField(ref,id,val);
}






JNIEXPORT jfloat JNICALL Memory(getRefFloat0)(JNIEnv* env, jclass, jobject ref, jlong offset) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	return env->GetFloatField(ref,id);
}

JNIEXPORT void JNICALL Memory(putRefFloat0)(JNIEnv* env, jclass, jobject ref, jlong offset, jfloat val) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	env->SetFloatField(ref,id,val);
}

JNIEXPORT jdouble JNICALL Memory(getRefDouble0)(JNIEnv* env, jclass, jobject ref, jlong offset) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	return env->GetDoubleField(ref,id);
}

JNIEXPORT void JNICALL Memory(putRefDouble0)(JNIEnv* env, jclass, jobject ref, jlong offset, jdouble val) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	env->SetDoubleField(ref,id,val);
}

JNIEXPORT jobject JNICALL Memory(getRefObject0)(JNIEnv* env, jclass, jobject ref, jlong offset) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	return env->GetObjectField(ref,id);
}


JNIEXPORT void JNICALL Memory(putRefObject0)(JNIEnv* env, jclass, jobject ref, jlong offset, jobject val) {
	jfieldID id = (jfieldID)(offset << ADD_SHIFT);
	env->SetObjectField(ref,id,val);
}

JNIEXPORT jint JNICALL Memory(getJNIVersion)(JNIEnv* env, jclass) {
	return env->GetVersion();
}


}