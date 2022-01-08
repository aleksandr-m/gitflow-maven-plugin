File file = new File( basedir, "build.log" );
assert file.exists();

String text = file.getText("utf-8");

// ensure build fails due to SNAPSHOT dependencies
assert text.contains("There is some SNAPSHOT dependencies in the project,")

return true;
