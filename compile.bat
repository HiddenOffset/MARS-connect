@echo off
REM Compile all Java files
for /r . %%f in (*.java) do (
    echo Compiling %%f
)
javac -d . Mars.java
for /r mars %%f in (*.java) do (
    javac -d . "%%f"
)
