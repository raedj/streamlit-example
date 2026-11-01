# On-device transcription: whisper.cpp (vendored, CPU only) behind a small JNI bridge.
cmake_minimum_required(VERSION 3.22)
project(polyglot_whisper C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(WHISPER_DIR ${CMAKE_CURRENT_SOURCE_DIR}/whisper)
# Always optimised, also in the app's debug build: unoptimised whisper.cpp needs minutes for a few
# seconds of speech (measured on the emulator), and its code is not debugged here.
set(CMAKE_BUILD_TYPE Release CACHE STRING "" FORCE)
set(CMAKE_C_FLAGS_RELEASE "-O3 -DNDEBUG" CACHE STRING "" FORCE)
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -DNDEBUG" CACHE STRING "" FORCE)

# Portable CPU code for every phone of an ABI (no -march=native when cross-compiling), no OpenMP,
# and ggml linked statically into the one library the app loads.
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_BACKEND_DL OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
add_subdirectory(${WHISPER_DIR}/ggml ggml)

add_library(polyglot_whisper SHARED
    polyglot_whisper.c
    ${WHISPER_DIR}/src/whisper.cpp
    ${WHISPER_DIR}/src/aneforge/whisper-aneforge.cpp)
target_include_directories(polyglot_whisper PRIVATE ${WHISPER_DIR}/include ${WHISPER_DIR}/src)
target_compile_definitions(polyglot_whisper PRIVATE WHISPER_VERSION="1.9.5")
target_link_libraries(polyglot_whisper ggml log android)

target_compile_options(polyglot_whisper PRIVATE -O3 -fvisibility=hidden -ffunction-sections -fdata-sections)
target_link_options(polyglot_whisper PRIVATE -Wl,--gc-sections -Wl,--exclude-libs,ALL)
# Android 15 devices may use 16 KB memory pages.
target_link_options(polyglot_whisper PRIVATE -Wl,-z,max-page-size=16384)
