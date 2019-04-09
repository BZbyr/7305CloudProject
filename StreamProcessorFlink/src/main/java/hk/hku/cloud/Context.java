package hk.hku.cloud;
/**
 * Created by IntelliJ IDEA
 * Project: CloudProject
 * Author: Boyang
 * Date: 2019-04-09 11:49
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
/**
 *
 *
 * @package: hk.hku.cloud
 * @class: Context
 * @author: Boyang
 * @date: 2019-04-09 11:49
 */
public class Context {
    private static final Logger LOG = LoggerFactory.getLogger(Context.class);

    private final Properties properties;

    Context(String configFileLocation) throws Exception {
        properties = new Properties();
        try {
            InputStream is = new FileInputStream(configFileLocation);
            properties.load(is);
        } catch (FileNotFoundException e) {
            LOG.error("Error : unable to find configuration file: "
                    + e.getMessage());
            throw e;
        } catch (SecurityException e) {
            LOG.error("Error: unauthorized to access configuration file: "
                    + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Error: unable to access configuration file: "
                    + e.getMessage());
            throw e;
        }
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }
}
