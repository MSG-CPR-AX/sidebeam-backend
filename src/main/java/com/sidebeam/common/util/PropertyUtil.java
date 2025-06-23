package com.sidebeam.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class PropertyUtil {

    private PropertyUtil() {
        super();
    }

    private static final String DELIMITER = ",";
    private static String applicationName = "";
    private static List<String> profiles;

    /**
     * 콤마로 열거된 문자열을 List로 변환
     * @MethodName  : readCsvToList
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : List<String>
     * @param commaSeparatedAliases
     * @return
     */
    public static List<String> readCsvToList(String commaSeparatedAliases) {
        return readCsvToList(commaSeparatedAliases, DELIMITER);
    }

    /**
     * 특정문자로 열거된 문자열을 List로 변환
     * @MethodName  : readCsvToList
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : List<String>
     * @param commaSeparatedAliases
     * @param delemiter
     * @return
     */
    public static List<String> readCsvToList(String commaSeparatedAliases, String delemiter) {
        return Arrays.stream(StringUtils.trimToEmpty(commaSeparatedAliases).split(delemiter))
                .filter(v -> !v.trim().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * spring.application.name 을 가져온다.
     * <PRE>
     * </PRE>
     * @MethodName  : getApplicationName
     * @Part        : 차세대 아키텍처
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 2. 16. 오후 2:54:52
     * @returnType  : String
     * @return
     */
    public static String getApplicationName() {
        if (StringUtils.isNotBlank(applicationName))
            return applicationName;

        /** 캐싱 */
        applicationName = getProperty("spring.application.name");
        return StringUtils.defaultIfBlank(applicationName, HttpRequestConstants.UNDEFINED_SERVICE);
    }

    /**
     * 동적으로 환경 변수 값을 가져온다.
     * <PRE>
     * </PRE>
     * @MethodName  : getProperty
     * @Part        : 차세대 아키텍처
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 2. 16. 오후 2:54:38
     * @returnType  : String
     * @param property
     * @return
     */
    public static String getProperty(String property) {
        try {
            Environment environment = ApplicationContextUtil.getApplicationContext().getEnvironment();
            return environment.getProperty(property);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * active prifile 이 매개변수로 준 값을 포함하고 있는지 체크한다.
     * <PRE>
     * 단, 체크사 endWith로 체크를 수행한다. (ex. sdev : SI개발서버, dev : 개발서버)
     * </PRE>
     * @MethodName  : hasAnyProfileByEndWith
     * @Part        : 차세대 아키텍처
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 2. 16. 오후 2:52:41
     * @returnType  : boolean
     * @param args
     * @return
     */
    public static boolean hasAnyProfileByEndWith(String... args) {
        try {
            /* 캐싱 */
            if (CollectionUtils.isEmpty(profiles)) {
                Environment environment = ApplicationContextUtil.getApplicationContext().getEnvironment();
                String[] localProfiles = environment.getActiveProfiles();
                if (localProfiles.length == 0) {
                    localProfiles = environment.getDefaultProfiles();
                }

                profiles = Arrays.asList(localProfiles);
            }

            if (CollectionUtils.isEmpty(profiles))
                return false;

            return profiles.stream()
                    .anyMatch(
                            profile -> Arrays.asList(args)
                                    .stream()
                                    .anyMatch(arg -> StringUtils.endsWithIgnoreCase(profile, arg)));

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Spring EL에서 profile값을 치환한다.
     * <PRE>
     * ex)
     * @Autowired
     * Environment environment;
     * PropertyUtil.environmentResolvePlaceholders("Application name is ${spring.application.name:not set}")
     * </PRE>
     * @MethodName  : environmentResolvePlaceholders
     * @Part        : 차세대 아키텍처
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2020. 9. 7. 오전 10:27:12
     * @returnType  : String
     * @param expressionString
     * @param environment
     * @return
     */
    public static String environmentResolvePlaceholders(String expressionString, Environment environment) {
        final String PROFILE_VARIABLE_PLACE_HOLDERS_PREFIX = "${";
        String resolvedString = expressionString;
        if (expressionString.indexOf(PROFILE_VARIABLE_PLACE_HOLDERS_PREFIX) > -1) {
            resolvedString = environment.resolvePlaceholders(expressionString);
        }
        return resolvedString;
    }

    /**
     * Spring EL 값로 값을 치환한다.
     * <PRE>
     * ex)
     * @Autowired
     * Environment environment;
     * PropertyUtil.environmentResolvePlaceholders("#{${app.var:-1}.equals(\"-1\")?\"true-value\":\"false-value\"}");
     * </PRE>
     * @MethodName  : springTemplateExpression
     * @Part        : 차세대 아키텍처
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2020. 9. 4. 오후 2:29:48
     * @returnType  : T
     * @param <T>
     * @param stringExpression
     * @return
     */
    public static String springTemplateExpression(String expressionString, Environment environment) {
        final String SPEL_TEMPLATE_HOLDERS_PREFIX = "#{";
        if (expressionString.indexOf(SPEL_TEMPLATE_HOLDERS_PREFIX) == -1)
            return expressionString;
        String resolvedExpression = environmentResolvePlaceholders(expressionString, environment);
        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression(resolvedExpression, ParserContext.TEMPLATE_EXPRESSION);
        return expression.getValue(String.class);
    }

    public static Resource getResource(String resourcePath) {
        return getResourceOrRetryOnHttp(resourcePath);
    }


    static Resource getResourceByNestedCatch(String resourcePath) {
        Resource resource;
        try {
            resource = new UrlResource(resourcePath);
            if (!resource.exists()) {
                throw new FileNotFoundException();
            }
        } catch (Exception e1) {
            try {
                PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver =
                        new PathMatchingResourcePatternResolver();
                resource = pathMatchingResourcePatternResolver.getResource(resourcePath);
                if (!resource.exists()) {
                    throw new FileNotFoundException(resourcePath);
                }
            } catch (Exception e2) {
                log.warn("resource load exception {} .. so attemp to load absolute path", e2.getMessage(), e2);

                Path filePath = Paths.get(resourcePath).toAbsolutePath().normalize();
                try {
                    resource = new UrlResource(filePath.toUri());
                } catch (MalformedURLException e3) {
                    throw new SysException(e3);
                }
            }
        }
        return resource;
    }

    /**
     * 디렉토리경로, file:, classpath:, http:, jar: 형식의 리소스를 생성한다.
     * Http Resource 인 경우 retry 를  수행한다.
     *
     * @param resourcePath 리소스경로
     * @return resource  resource.exist() 를 체크하지 않으므로, 리소스가 없다면, getInputStream()으로 리소스 오픈시 오류가 발생한다.
     */
    private static Resource getResourceOrRetryOnHttp(String resourcePath) {

        if(StringUtils.startsWithAny(resourcePath, new String[] {ResourceUtils.CLASSPATH_URL_PREFIX, ResourceUtils.FILE_URL_PREFIX})) {
            return new PathMatchingResourcePatternResolver().getResource(resourcePath);
        } else if(StringUtils.startsWithAny(resourcePath, new String[]{ResourceUtils.JAR_URL_PREFIX})) {
            try {
                return new UrlResource(resourcePath);
            } catch (MalformedURLException e) {
                throw new WaffulException(e);
            }
        } else if(StringUtils.startsWithAny(resourcePath, new String[]{"http:", "https:"})) {
            try {
                return new RetryableUrlResource(resourcePath);
            } catch (IOException e) {
                throw new WaffulException(e);
            }
        } else {
            Resource resource = new PathMatchingResourcePatternResolver().getResource(resourcePath);
            // schema 없이 class path
            // ex. config/config.properties or /config/config.properties
            if(resource.exists()) {
                return resource;
            } else {
                // absolute file path 인지 다시 한번 확인 한다.
                Path filePath = Paths.get(resourcePath).toAbsolutePath().normalize();
                try {
                    return new UrlResource(filePath.toUri());
                } catch (MalformedURLException e3) {
                    throw new SysException(e3);
                }
            }
        }
    }



    /**
     * yaml properties 를 object mapping 하여 로딩 한다.
     * <PRE>
     * 주의 사항 :
     *   dot notation(nuxx.url : /abc/def) 을 지원되지 않는다. yaml표준에 따라 nuxx: 와 url: 로 분리해야한다.
     *   dot notation을 사용하고자 할 경우 {@link PropertyUtil.getYmlProperties(String, ResolvableType )}를 사용한다.
     * ex)
     *  // classpath loading
     *  String path = "classpath:/config/domain.yml";
     *  // config server resource loading :  http://config-server/{service}/{profile}/{branch}/{path}/{file-name}
     *  String path = "http://nucf.apps.dprv-paas.lguplus.co.kr/nubo-svc/dev/master/config/domain.yml"
     *  DomainProperties domain = PropertyUtil.getYmlProperties(path, new TypeReference&ltDomainProperties&gt() {});
     * </PRE>
     * @MethodName  : getYmlProperties
     * @Part        : 차세대 아키텍처
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2020. 9. 2. 오후 2:05:59
     * @returnType  : T
     * @param <T>
     * @param path
     * @param typeReference
     * @return
     */
    public static <T> T getYmlProperties(String path, TypeReference<T> typeReference) {
        T result;
        try {
            Resource resource = getResource(path);
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
            mapper.findAndRegisterModules();
            try(InputStream inputStream = resource.getInputStream()) {
                result = mapper.readValue(inputStream, typeReference);
            }
        } catch (IOException e) {
            throw new WaffulException(e);
        }

        return result;
    }

    /**
     * Yaml 파일을 읽고 Object 바인딩한 객체를 반환한다.
     * <pre>
     * 참고 사항 : dot notation(nuxx.url : /abc/def) 을 지원된다.
     * ex)
     *  // classpath loading
     *  String path = "classpath:/config/sample.yml";
     *  // 로딩된 yaml 파일에서 특정속성하위만 로딩할 경우
     *  SampleProp sample = PropertyUtil.getYmlProperties(path, "sample", SampleProp.class);
     *  // 로딩된 yaml 파일 전체를 로딩할 경우
     *  SampleDto sample = PropertyUtil.getYmlProperties(path, "", SampleProp.class);
     * </pre>
     *
     * @MethodName  : getYmlProperties
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : T
     * @param <T>
     * @param path
     * @param prefix
     * @param targetClass
     * @return
     */
    public static <T> T getYmlProperties(String path, String prefix, Class<T> targetClass) {
        Properties properties = getYmlProperties(path);
        return fromProperties(properties, prefix, targetClass);
    }

    /**
     * Yaml 파일을 읽고 Object 바인딩한 객체를 반환한다.
     * <pre>
     * 참고 사항 : dot notation(nuxx.url : /abc/def) 을 지원된다.
     * ex)
     *  // classpath loading
     *  String path = "classpath:/config/sample.yml";
     *  SampleDto sample = PropertyUtil.getYmlProperties(path, SampleProp.class);
     * </pre>
     *
     * @MethodName  : getYmlProperties
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : T
     * @param <T>
     * @param path
     * @param targetClass
     * @return
     */
    public static <T> T getYmlProperties(String path, Class<T> targetClass) {
        Properties properties = getYmlProperties(path);
        return fromProperties(properties, null, targetClass);
    }

    /**
     * Yaml 파일을 읽고 Object 바인딩한 객체를 반환한다.
     * <pre>
     * 참고 사항 : dot notation(nuxx.url : /abc/def) 을 지원된다.
     * ex)
     *  // classpath loading
     *  String path = "classpath:/config/sample.yml";
     *  ResolvableType type = ResolvableType.forClassWithGenerics(HashMap.class, String.class, SampleProp.class);
     *  // 로딩된 yaml 파일에서 특정속성하위만 로딩할 경우
     *  Map<String,SampleProp> sample = PropertyUtil.getYmlProperties(path, "sample", type);
     *  // 로딩된 yaml 파일 전체를 로딩할 경우
     *  Map&lt;String,SampleProp&gt; sample = PropertyUtil.getYmlProperties(path, "", type);
     * </pre>
     *
     * @MethodName  : getYmlProperties
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : T
     * @param <T>
     * @param path
     * @param prefix
     * @param resolvableType
     * @return
     */
    public static <T> T getYmlProperties(String path, String prefix, ResolvableType resolvableType) {
        Properties properties = getYmlProperties(path);
        return fromProperties(properties, prefix, resolvableType);
    }

    /**
     * Yaml 파일을 읽고 Object 바인딩한 객체를 반환한다.
     * <pre>
     * 참고 사항 : dot notation(nuxx.url : /abc/def) 을 지원된다.
     * ex)
     *  // classpath loading
     *  String path = "classpath:/config/sample.yml";
     *  ResolvableType type = ResolvableType.forClassWithGenerics(HashMap.class, String.class, SampleProp.class);
     *  Map&lt;String,SampleProp&gt; sample = PropertyUtil.getYmlProperties(path, type);
     * </pre>
     *
     * @MethodName  : getYmlProperties
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : T
     * @param <T>
     * @param path
     * @param resolvableType
     * @return
     */
    public static <T> T getYmlProperties(String path, ResolvableType resolvableType) {
        Properties properties = getYmlProperties(path);
        return fromProperties(properties, null, resolvableType);
    }

    /**
     * Yaml 파일을 읽고 Properties 로 반환한다.
     * <pre>
     * 참고 사항 : dot notation(nuxx.url : /abc/def) 을 지원된다.
     * ex)
     *  // classpath loading
     *  String path = "classpath:/config/sample.yml";
     *  Properties sample = PropertyUtil.getYmlProperties(path)
     * </pre>
     *
     * @MethodName  : getYmlProperties
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : Properties
     * @param path
     * @return
     */
    public static Properties getYmlProperties(String path) {
        try {
            Resource resource = PropertyUtil.getResource(path);
            return new YamlPropertiesProcessor(resource).createProperties();
        } catch (IOException e) {
            log.error("com.lguplus.wafful.framework.util.PropertyUtil.getYmlProperties(String, Class<T>)", e);
            throw new WaffulException(e);
        }
    }

    /**
     * Properties를 객체에 바인당하여 반환한다.
     *
     * @MethodName  : fromProperties
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : T
     * @param <T>
     * @param properties
     * @param prefix
     * @param targetClass
     * @return
     */
    public static <T> T fromProperties(Properties properties, String prefix, Class<T> targetClass) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        return new Binder(source).bindOrCreate(StringUtils.defaultIfBlank(prefix, ""), targetClass);
    }

    /**
     * Properties를 객체에 바인당하여 반환한다.
     *
     * @MethodName  : fromProperties
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : T
     * @param <T>
     * @param properties
     * @param prefix
     * @param resolvableType
     * @return
     */
    public static <T> T fromProperties(Properties properties, String prefix, ResolvableType resolvableType) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        return new Binder(source).bindOrCreate(StringUtils.defaultIfBlank(prefix, ""), Bindable.of(resolvableType));
    }

    /**
     * Properties를 객체에 set 한다
     *
     * @MethodName  : setProperties
     * @Part        : 차세대UCUBE SWA
     * @Author      : 서현호(wisesky1@lgupluspartners.co.kr)
     * @ModifiedDate: 2021. 7. 19.
     * @type        : void
     * @param <T>
     * @param properties
     * @param prefix
     * @param instance
     */
    public static <T> void setProperties(Properties properties, String prefix, T instance) {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        new Binder(source).bind(StringUtils.defaultIfBlank(prefix, ""), Bindable.ofInstance(instance));
    }

    /**
     * <PRE>
     *  Class Name    : PropertyUtil
     *  Method Name   : getProperties
     *  Comment       : properties파일을 읽어서 map형태로 반환한다.
     *  예> Map<String, String> mResult = PropertyUtil.getProperties("config/masking.properties");
     *     String thisValue = mResult.get("userNm");
     *
     * </PRE>
     *
     * @Author tjleeswa
     * @Date 2019. 8. 30. 오후 3:47:41
     * @Tag @param propertiesFile 읽고자 하는 properties 파일의 경로
     * @Tag @return
     */
    @SuppressWarnings({"java:S1319"})
    public static HashMap<String, String> getProperties(String propertiesFile) {
        Properties properties = new Properties();
        HashMap<String, String> map = new HashMap<>();

        try {
            PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();

            Resource resource = getResource(propertiesFile);
            propertiesFactoryBean.setLocation(resource);
            propertiesFactoryBean.afterPropertiesSet();

            properties = propertiesFactoryBean.getObject();
            Assert.notNull(properties, propertiesFile + " properties is null object");

            for (String key : properties.stringPropertyNames()) {
                map.put(key, properties.getProperty(key));
            }

            log.trace("properties is loaded {} : {}", propertiesFile, map);

        } catch (IOException e) {
            throw new WaffulException(e);
        }
        return map;
    }

}

