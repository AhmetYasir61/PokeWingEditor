cmake_minimum_required(VERSION 3.22.1)
project("pokewinglauncher")

# Native katman iki gorev ustlenir:
#  1) jvm_bridge   : JNI Invocation API (JNI_CreateJavaVM) uzerinden gomulu bir
#                    JRE/JDK (ornegin JetBrains/Android icin derlenmis OpenJDK)
#                    baslatmak ve Minecraft main() cagrisini tetiklemek.
#  2) gl_optimizer  : LWJGL/OpenGL ES kopru katmani + dusuk donanim icin
#                     frame-pacing / texture-cache optimizasyonlari.
add_library(jvm_bridge SHARED
        jvm_bridge.cpp)

add_library(gl_optimizer SHARED
        gl_optimizer.cpp)

find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv3)

target_link_libraries(jvm_bridge
        ${log-lib}
        ${android-lib})

target_link_libraries(gl_optimizer
        ${log-lib}
        ${android-lib}
        ${gles-lib})

target_compile_options(jvm_bridge PRIVATE -O3 -fvisibility=hidden)
target_compile_options(gl_optimizer PRIVATE -O3 -fvisibility=hidden)
