package gs.com.gses.config;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Configuration
//@ConfigurationProperties(prefix = "elastic-search")
@EnableElasticsearchRepositories(
        basePackages = "gs.com.gses.elasticsearch")//Repository 路径
public class ElasticsearchRestClientConfig extends AbstractElasticsearchConfiguration {

    private static final Logger LOGGER = LogManager.getLogger(ElasticsearchRestClientConfig.class);
    //D:\work\software\es\elasticsearch-8.0.0\config\certs 目录下http_ca.crt
    // 或者到kibana路径下去找D:\work\software\es\kibana-8.0.0\data
//    String certificateBase64 = "MIIFWjCCA0KgAwIBAgIVAIiLIjhyvKau0DZtLOLiPngl/7j2MA0GCSqGSIb3DQEB\n" +
//            "CwUAMDwxOjA4BgNVBAMTMUVsYXN0aWNzZWFyY2ggc2VjdXJpdHkgYXV0by1jb25m\n" +
//            "aWd1cmF0aW9uIEhUVFAgQ0EwHhcNMjIwMzA4MTA0NjAyWhcNMjUwMzA3MTA0NjAy\n" +
//            "WjA8MTowOAYDVQQDEzFFbGFzdGljc2VhcmNoIHNlY3VyaXR5IGF1dG8tY29uZmln\n" +
//            "dXJhdGlvbiBIVFRQIENBMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA\n" +
//            "xF5oz4V9DV15X2VEIokOKBbzVNE+qQxMoeupRjaDsQ2FaC0XtrSFMozvHNiVvsP/\n" +
//            "qzOe08zFCK0hYN8ri0GpzVSYvNgZ4HMsHI8KrdziC27HTkBqPfa0HBaIScSOumta\n" +
//            "uhW4gJWxXcZp+H7DMlsiuKlo2XOhMvZgiMCMZqL8x5rx7Q87muWoULULNpdChqbp\n" +
//            "LmlusWHYdjJa74ugzcwPW5iVyy4yJdgcrn6Y8i9LJwJB4QC3YJCaHW7jaY+XQz37\n" +
//            "l71vTJfy9+EHIjW5ytlGZvZKtcvX4Q30l+X5/ubM35zfCumR7PP4X3fVqH/BNc4v\n" +
//            "mwZ4qgA7NfCiJxwdrXB4mz8jhRGNr37/I+mSH8zLpe4usdzJG6WJJH45B3vM1ahW\n" +
//            "yBvXNi+fgjcuw1KpjxeKDrpfGcS260/9YZ3Jrid8LAwUsF7nzsFDKTCbowb05EiS\n" +
//            "udbBmEOeraMcX736O4mdT1bNofLKbbHODDuw6qK1xFjJ7nSGOeD7i3pkLwsoYIFC\n" +
//            "AKx5zI2+dvtqscbQ/GExjW+JaiiTJIU5+3UHokxucIFrLSSuKFQNtva/FcfBSwYb\n" +
//            "wzE7gos9z7r69wwW4t2K5SP+atSq+sUycoVoSlDMIcnNRSfYAQG5YNIsWD6oVj0K\n" +
//            "oEhuzl3A3BoS4DCP8Jq9UagZC8xmdIme/w4LzK8j3tUCAwEAAaNTMFEwHQYDVR0O\n" +
//            "BBYEFHz7PDYJ09CujDs9OUSwdXsdGBO4MB8GA1UdIwQYMBaAFHz7PDYJ09CujDs9\n" +
//            "OUSwdXsdGBO4MA8GA1UdEwEB/wQFMAMBAf8wDQYJKoZIhvcNAQELBQADggIBAKVZ\n" +
//            "uaDIWEZp6mnQEKLm0jTmhrSPZSC2D5nfnsXFCxvG/bjvWdfxwlTUMYu/qTlAlCjG\n" +
//            "o9HjdVHPkJBcFbeni67TNl6T6YYdCWl0HzIMJYeGMS96g/DdA80qW+s3f5c1/4vg\n" +
//            "JYXm6L4yA31sxi0EqJirPOUZ5hswVpyJxZ0D2iS/x7mLCRUkTPzMYd25PEB5ql+C\n" +
//            "L/a3ILgSBZm724Yic5jc/vTXlQhPAW1sQ4JFED2X4y05bGEuAt2EFxFghbx1ElsT\n" +
//            "PgPqiL1aTBl55d7LNUthTFK+tDaO1mj1+XYmuBMYiq/VY/U17HK8Sxq+3ZrlHMqP\n" +
//            "OW7990g/7ffgPVRZYAK9FfUQZpy0ls5FwNo/5DvdXEveBW+srW/uqqP/+dhgyA8X\n" +
//            "/xFxIDBN/I2o9v8sQIQCBgEsshgQN1HpFyXEtcrB3zZDoZ/D+822roF2kmyioM/l\n" +
//            "e5vZlhbCrgSkp217mspTaGawv4jcdkFFB7AtigH3Dfb/C0is58xH06W6DoLzysbI\n" +
//            "XeqR+gGaFmWbvwWoyH87BQgpZni5j5m63x1VeBB0LX2gz+a+oFlhdaed+khVOQ4l\n" +
//            "900n+h6GhcDPC0ARbzGNr9lB64tsQTE5+ZKxpeORiNwUQWqhK5jKv9iCyeQS/4qv\n" +
//            "N7LYCILrsXsHbk2gic0C8K38jv6jfOndwgBQs4GP";


    //    @Value("${sbp.ESCertPath}")
//    private String eSCertPath;
//
    @Value("${sbp.hostAndPort}")
    private String hostAndPort;


//    @Override
//    @Bean
//    public RestHighLevelClient elasticsearchClient() {
//
//
////        File file = new File(eSCertPath);
////        List<String> lines = new ArrayList<>();
////        String certificateBase64 = "";
////        try {
////            lines = FileUtils.readLines(file, "UTF-8");
////            StringBuilder stringBuilder = new StringBuilder();
////           for(int i=0;i<lines.size();i++)
////           {
////               if(i==0||i==lines.size()-1)
////               {
////                   continue;
////               }
////               stringBuilder.append(lines.get(i));
////           }
////            certificateBase64 = stringBuilder.toString();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
//
//
//
//
//        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//               //127 连不上就换IP 127.0.0.1   192.168.8.85
//                .connectedTo(hostAndPort)
//                //ES8需要下面的SSL配置
////                .usingSsl(this.getSSLContext(certificateBase64))
////                .withBasicAuth("elastic", "==Qok*0raTpVzjnvv_dr")
//                //默认5s,批量插入超时异常： 5,000 milliseconds timeout on connection http-outgoing-0 [ACTIVE];
//                .withSocketTimeout(60000)//默认30s
//                .withConnectTimeout(10000)//默认1s
////                .withConnectTimeout(Duration.ofSeconds(5))
////                .withSocketTimeout(Duration.ofSeconds(60))
//
//                .build();
//
//        return RestClients.create(clientConfiguration).rest();
//
//
//
//    }

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .withConnectTimeout(Duration.ofSeconds(10))
                .withSocketTimeout(Duration.ofSeconds(60))
                .withClientConfigurer((ClientConfiguration.ClientConfigurationCallback<RestClientBuilder>) restClientBuilder -> {
                    // 设置连接池参数
                    restClientBuilder.setHttpClientConfigCallback(httpClientBuilder ->
                            httpClientBuilder
                                    .setMaxConnTotal(100)
                                    .setMaxConnPerRoute(20)

                    );
                    // 设置请求超时参数
                    restClientBuilder.setRequestConfigCallback(requestConfigBuilder ->
                            requestConfigBuilder
                                    .setConnectionRequestTimeout(3000)
                                    .setConnectTimeout(10000)
                                    .setSocketTimeout(60000)
                    );
                    return restClientBuilder;
                })

                .build();

        return RestClients.create(clientConfiguration).rest();
    }


//    @Bean
//    @Override
//    public EntityMapper entityMapper() {
//        ElasticsearchEntityMapper entityMapper = new ElasticsearchEntityMapper(
//                elasticsearchMappingContext(), new DefaultConversionService());
//        entityMapper.setConversions(elasticsearchCustomConversions());
//
//        return entityMapper;
//    }


    private SSLContext getSSLContext(String base64Cert) {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            InputStream is = new ByteArrayInputStream(Base64.getMimeDecoder().decode(base64Cert));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);
            ks.setCertificateEntry("caCert", caCert);
            tmf.init(ks);
            sslContext.init(null, tmf.getTrustManagers(), null);
        } catch (Exception e) {
            // logService.error(getClass(), "load ssl error." + e.getMessage());
        }
        return sslContext;
    }


}
