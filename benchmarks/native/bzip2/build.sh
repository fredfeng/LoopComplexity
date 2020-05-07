wget http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz
tar -xzf bzip2-1.0.6.tar.gz
rm bzip2-1.0.6.tar.gz
cd bzip2-1.0.6
patch < ../patches.diff

# Apply Makefile patches (eg. CC & CFLAGS)
# make
