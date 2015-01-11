The PDE target platform API changed between 3.7 and 3.8. Therefore the TPHelper class abstracts from the concrete
implementations TPHelperImpl37 and TPHelperImpl38. These two classes must be compiled in the corresponding Eclipse
version. Therefore the implementations are in the two Jars in the lib folder and the TPHelper loads the correct one
depending on the PDE version. The original sources are in this package but renamed so that they don't get compiled. In
order to change an implementation, open the project with the correct Eclipse version, rename the file, change it, and
export it into the correct Jar.
