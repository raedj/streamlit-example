# On-device models: whisper.cpp (transcription) and llama.cpp (chat), vendored CPU-only, sharing one
# ggml (llama.cpp's copy, the newer one) inside a single library behind small JNI bridges.
cmake_minimum_required(VERSION 3.22)
project(polyglot_ml C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(WHISPER_DIR ${CMAKE_CURRENT_SOURCE_DIR}/whisper)
set(LLAMA_DIR ${CMAKE_CURRENT_SOURCE_DIR}/llama)
# Always optimised, also in the app's debug build: unoptimised, a few seconds of speech took minutes
# (measured on the emulator), and this code is not debugged here.
set(CMAKE_BUILD_TYPE Release CACHE STRING "" FORCE)
set(CMAKE_C_FLAGS_RELEASE "-O3 -DNDEBUG" CACHE STRING "" FORCE)
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG" CACHE STRING "" FORCE)

# Portable CPU code for every phone of an ABI (no -march=native when cross-compiling), no OpenMP,
# and ggml linked statically into the one library the app loads.
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_BACKEND_DL OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
# x86_64 (emulators, Chromebooks): AVX2/FMA/F16C, about an order of magnitude faster than plain code.
# The app checks /proc/cpuinfo before loading the library and leaves the engines off on CPUs without them.
# ARM builds keep the portable baseline (NEON is always there on arm64).
if (ANDROID_ABI STREQUAL "x86_64")
    foreach(flag GGML_SSE42 GGML_AVX GGML_AVX2 GGML_FMA GGML_F16C)
        set(${flag} ON CACHE BOOL "" FORCE)
    endforeach()
endif ()
add_subdirectory(${LLAMA_DIR}/ggml ggml)

# llama.cpp core (its own src/CMakeLists.txt is not vendored).
file(GLOB LLAMA_SOURCES ${LLAMA_DIR}/src/*.cpp ${LLAMA_DIR}/src/models/*.cpp)
set(LLAMA_VERSION "0.7.0")
set(LLAMA_BUILD_COMMIT "v0.7.0")
configure_file(${LLAMA_DIR}/src/llama-version.h.in ${CMAKE_CURRENT_BINARY_DIR}/llama-version.h @ONLY)

add_library(polyglot_ml SHARED
    polyglot_whisper.c
    polyglot_llama.cpp
    ${WHISPER_DIR}/src/whisper.cpp
    ${WHISPER_DIR}/src/aneforge/whisper-aneforge.cpp
    ${LLAMA_SOURCES})
target_include_directories(polyglot_ml PRIVATE
    ${WHISPER_DIR}/include ${WHISPER_DIR}/src
    ${LLAMA_DIR}/include ${LLAMA_DIR}/src ${CMAKE_CURRENT_BINARY_DIR})
target_compile_definitions(polyglot_ml PRIVATE WHISPER_VERSION="1.9.5")
target_link_libraries(polyglot_ml ggml log android)

target_compile_options(polyglot_ml PRIVATE -O3 -fvisibility=hidden -ffunction-sections -fdata-sections)
target_link_options(polyglot_ml PRIVATE -Wl,--gc-sections -Wl,--exclude-libs,ALL)
# Android 15 devices may use 16 KB memory pages.
target_link_options(polyglot_ml PRIVATE -Wl,-z,max-page-size=16384)
