pjsip_patches := $(wildcard jni/pjsip/patches/*.diff)


all : libraries
	# Dispatch to external projects
	./dispatch_shared_libs.sh

libraries : ext-sources ext-lib swig-glue
	# Build main libraries using android ndk
	ndk-build -j4

ext-lib :
	# Build ffmpeg using make
	cd jni/ffmpeg; $(MAKE) $(MFLAGS)

ext-sources : jni/silk/sources jni/zrtp4pj/sources jni/openssl/sources jni/pjsip/.patched_sources
	# External sources fetched out from external repos/zip

swig-glue : 
	cd jni/swig-glue; $(MAKE) $(MFLAGS)

## External resources from repos/zip ##
jni/silk/sources :
	cd jni/silk; \
	wget http://developer.skype.com/silk/SILK_SDK_SRC_v1.0.8.zip; \
	unzip -q SILK_SDK_SRC_v1.0.8.zip; \
	mv SILK_SDK_SRC_v1.0.8 sources; \
	rm SILK_SDK_SRC_v1.0.8.zip

jni/zrtp4pj/sources :
	cd jni/zrtp4pj; \
	git clone git://github.com/r3gis3r/ZRTP4PJ.git sources

jni/openssl/sources :
	cd jni/openssl; \
	git clone git://github.com/guardianproject/openssl-android.git sources

jni/pjsip/.patched_sources : $(pjsip_patches)
	cd jni/pjsip && \
	quilt push -a || \
	touch .patched_sources

clean :
	ndk-build clean
	

update :
	if [ -f jni/pjsip/.patched_sources ]; then cd jni/pjsip && quilt pop -af || rm .patched_sources; cd -; fi;
	svn update --accept theirs-conflict
	# Update ZRTP4pj
	cd jni/zrtp4pj/sources; \
	git checkout 596346dc346471817db103d75af66b8ce69dca79
	# Update OpenSSL
	cd jni/openssl/sources; \
	git checkout 1a3c5799337b90ddc56376ace7284a9e7f8cc988
	