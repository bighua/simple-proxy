(jde-project-file-version "1.0")
(jde-set-variables
 '(jde-project-name "Jcm-Project-Template")
 ;; classpath
 '(jde-global-classpath 
   (quote 
    ("e:/gits/Jcm-Project-Template/lib/*"
     "e:/gits/Jcm-Project-Template/bin/"
     "$ANT_HOME/lib/*")
    ))
 ;; 在编译时用到
 '(jde-compile-option-sourcepath 
   (quote ("e:/gits/Jcm-Project-Template/src" 
	   "e:/gits/Jcm-Project-Template/test")))
 '(jde-compile-option-classpath 
   (quote ("e:/gits/Jcm-Project-Template/lib/*" 
	   "e:/gits/Jcm-Project-Template/bin/")))
 ;; Junit
 '(jde-junit-working-directory "e:/gits/Jcm-Project-Template/")
 '(jde-run-working-directory "e:/gits/Jcm-Project-Template/")
 '(jde-sourcepath 
   (quote ("e:/gits/Jcm-Project-Template/src/" 
	   "e:/gits/Jcm-Project-Template/test")))
 ;;'(jde-run-application-class "e:/gits/Jcm-Project-Template/bin")
 ;;'(jde-run-working-directory "e:/gits/Jcm-Project-Template")
 '(jde-compile-option-directory "e:/gits/Jcm-Project-Template/bin/")
 '(jde-compile-option-encoding "utf-8")
 '(jde-build-function (quote (jde-ant-build)))
 '(jde-ant-enable-find t)
 '(jde-ant-read-target t)
 '(jde-ant-home "$ANT_HOME")
 '(jde-ant-invocation-method (quote ("Ant Server")))
 ;;'(jde-ant-user-jar-files (quote ("")) ; 这里对应eclipse中add build里的jars
 ;;'(jde-vm-path "/Java/jdk1.6/bin/java")
 ;;'(jde-run-application-class "e:/gits/Jcm-Project-Template/bin")
 )
