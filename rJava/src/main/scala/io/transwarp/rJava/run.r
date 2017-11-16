if(!require(MASS)) install.packages("MASS")
if(!require(recharts))
{
    install.packages("~/package_recharts/htmltools_0.3.5.tar.gz", repos = NULL, type = "source")
    install.packages("~/package_recharts/htmlwidgets_0.6.tar.gz", repos = NULL, type = "source")
    install.packages("~/package_recharts/httpuv_1.3.3.tar.gz", repos = NULL, type = "source")
    install.packages("~/package_recharts/yaml_2.1.13.tar.gz", repos = NULL, type = "source")
    install.packages("~/package_recharts/mime_0.4.tar.gz", repos = NULL, type = "source")
    install.packages("~/package_recharts/shiny_0.13.2.tar.gz", repos = NULL, type = "source")
    install.packages("~/package_recharts/recharts_0.1.0.tar.gz", repos = NULL, type = "source")
}

require(MASS)
library(recharts)

## 读取数据
sc <- discover.init(passwd = "123456",inceptor_mode = "kerberos")
rdd <- txTextFile(sc, path = "/tmp/tmp2.txt")
data <- txCollect(rdd, sep = ",", col.type = c("double", "double"))
shanghai <- data

## 商户分布热力图
bMap(shanghai,type = "heatmap")

## 散点图（不要大于30000个点）
bMap(shanghai,type = "point")

## Kmeans 聚类图，使用Kmeans对上海地区商家的经纬度进行聚类分析，聚成15个类
shanghai_kmeans = kmeans(as.matrix(shanghai), 15)
shanghai$group = as.factor(shanghai_kmeans$cluster)
bMap(shanghai,type = "cluster")

## 通过Scala/Java编写rJava程序
library(rhdfs)
hdfs.init()

object <- 'rJava2.jar'

txAddJar(sc, object, "/home/discover", "/tmp/kkk", option = 1, overwrite = FALSE)
#J函数(类名，方法名，方法需要传入的参数)
label <- J("io.transwarp.rJava","run", sc, "/tmp/tmp2.txt", 15)

rdd2 <- txTextFile(sc, path = "/tmp/tmp2.txt")
data2 <- txCollect(rdd, sep = " ", col.type = c("double", "double"))
data2$group <- txCollect(label, sep=" ")

## Kmeans 聚类图，使用Kmeans对上海地区商家的经纬度进行聚类分析，聚成15个类
bMap(data2,type = "cluster")