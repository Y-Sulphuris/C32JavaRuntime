cmake_minimum_required(VERSION 3.27)
project(native)

set(CMAKE_CXX_STANDARD 14)

include_directories(.)
include_directories(smmalloc)

include_directories(C:/Users/lidia/.jdks/openjdk-18.0.1.1/include)
include_directories(C:/Users/lidia/.jdks/openjdk-18.0.1.1/include/win32)

add_library(native SHARED
        smmalloc/smmalloc.cpp
        smmalloc/smmalloc.h
        smmalloc/smmalloc_generic.cpp
        smmalloc/smmalloc_tls.cpp
        c32_memory_Memory.h
        c32_memory_SMMalloc.h
        c32_memory_SMMalloc_CacheWarmupOptions.h
        Memory.cpp)
