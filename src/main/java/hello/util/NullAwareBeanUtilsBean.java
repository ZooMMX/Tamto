package hello.util;

import org.apache.commons.beanutils.BeanUtilsBean;

import java.lang.reflect.InvocationTargetException;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 16/05/15
 * Time: 13:01
 */
public class NullAwareBeanUtilsBean extends BeanUtilsBean {

    @Override
    public void copyProperty(Object dest, String name, Object value)
            throws IllegalAccessException, InvocationTargetException {
        if(value==null)return;
        super.copyProperty(dest, name, value);
    }

}
