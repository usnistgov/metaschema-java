File genSources = new File(basedir, 'target/generated-sources/metaschema/');
assert genSources.exists(), "Didn't find generated sources [" + genSources.getAbsolutePath() + "]";