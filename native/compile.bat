:: D:\__Sys__\Users\lidia\Local\JetBrains\Toolbox\apps\CLion\ch-0\231.9011.31\bin\cmake\win\x64\bin\cmake.exe --build G:\Sulphuris\WMap\jni --target wmap -j 6
clang++ -shared -std=c++20  ^
-I C:/Users/lidia/.jdks/openjdk-18.0.1.1/include -I C:/Users/lidia/.jdks/openjdk-18.0.1.1/include/win32 -target x86_64-windows -m32 ^
 Memory.cpp smmalloc/smmalloc.cpp smmalloc/smmalloc_generic.cpp smmalloc/smmalloc_tls.cpp ^
  -O1    -o windows/c32rt.dll

clang++ -shared -std=c++20 ^
-I C:/Users/lidia/.jdks/openjdk-18.0.1.1/include -I C:/Users/lidia/.jdks/openjdk-18.0.1.1/include/win32 -target x86_64-windows ^
 Memory.cpp smmalloc/smmalloc.cpp smmalloc/smmalloc_generic.cpp smmalloc/smmalloc_tls.cpp ^
  -O1    -o windows/c32rt_64.dll
:: clang++ -shared -std=c++20 -stdlib=libstdc++ ^
::  -I C:/Users/lidia/.jdks/openjdk-18.0.1.1/include^
::  -I C:/Users/lidia/.jdks/openjdk-18.0.1.1/include/linux^
::  -I D:/__Sys__/Users/lidia/Local/JetBrains/Toolbox/apps/CLion/ch-0/232.9921.42/bin/mingw/lib/gcc/x86_64-w64-mingw32/13.1.0/include/c++^
::  -I D:/__Sys__/Users/lidia/Local/JetBrains/Toolbox/apps/CLion/ch-0/232.9921.42/bin/mingw/lib/gcc/x86_64-w64-mingw32/13.1.0/include/c++/bits^
::  -I D:/__Sys__/Users/lidia/Local/JetBrains/Toolbox/apps/CLion/ch-0/232.9921.42/bin/mingw/x86_64-w64-mingw32/include^
::  -I D:/__Sys__/Users/lidia/Local/JetBrains/Toolbox/apps/CLion/ch-0/232.9921.42/bin/mingw/lib/gcc/x86_64-w64-mingw32/13.1.0/include/c++/x86_64-w64-mingw32/bits^
::  -target x86_64-linux library.cpp smmalloc/smmalloc.cpp smmalloc/smmalloc_generic.cpp smmalloc/smmalloc_tls.cpp -o linux/libwmap.so
:: clang++ -dynamiclib -std=c++20 -stdlib=libstdc++  -I C:/Users/lidia/.jdks/openjdk-18.0.1.1/include -I C:/Users/lidia/.jdks/openjdk-18.0.1.1/include/darwin -target x86_64-apple-macosx10.15.0 library.cpp smmalloc/smmalloc.cpp smmalloc/smmalloc_generic.cpp smmalloc/smmalloc_tls.cpp -o macosx/libwmap.dylib

mv windows/c32rt_64.dll ../src/main/resources/native/windows/c32rt.dll
mv windows/c32rt_64.dll ../src/main/resources/native/windows/c32rt_64.dll
::-dynamiclib

PAUSE
