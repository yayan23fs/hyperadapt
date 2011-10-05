This project holds example classes and configuration files for embedding pxweave into a maven based 
cocoon project. Since the implementation is based on aspectj, make sure that the adaptation aspect
is woven accordingly into your cocoon project. The easiest way to do so is to just copy the classes
in net.hyperadapt.pxweave.integration.cocoon into your source folder (the package structure should be 
the same). Afterwards you have to add the weaver setup as a cocoon action to your own sitemap.xmap. 
See 'example.sitemap.xmap' to get an idea on how to do this.