/* Function.java

{{IS_NOTE
	Purpose:
		
	Description:
		
	History:
		Jun 23, 2012 4:39:34 PM , Created by jumperchen
}}IS_NOTE

Copyright (C) 2012 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
package org.zkoss.openlayers.util;

import java.util.LinkedList;
import java.util.List;

import org.zkoss.json.JSONAware;
import org.zkoss.json.JSONValue;
import org.zkoss.openlayers.Openlayers;

/**
 * A Javascript Object
 * @author jumperchen
 */
public class Function implements JSONAware {
    protected String _native;//客户端类型
    protected Object[] _arguments;//客户端对象的构造器参数
    protected List<Method> _methodQueue;

    public Function(String nativeClass, Object... arguments) {
        _native = nativeClass;
        _arguments = arguments;
        _methodQueue = new LinkedList<Method>();
    }

    public Function invoke(String method, Object... arguments) {
        _methodQueue.add(new Method(method, arguments));
        return this;
    }

    public String toJSONString(Openlayers map) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("(function (){ var _a =  new ").append(_native).append('(');
        if (_arguments.length > 0) {
            for (Object arg : _arguments) {
                sb.append(JSONValue.toJSONString(arg)).append(',');
            }
            int len = sb.length();
            sb.deleteCharAt(len - 1);
        }
        sb.append(");");//用参数创建一个客户端对象，名为 _a 。
        if (!_methodQueue.isEmpty()) {
            for (Method method : _methodQueue) {
                //依次调用客户端对象上的方法
                sb.append(" _a['").append(method.getName()).append("'].apply(_a,")//
                        .append(JSONValue.toJSONString(method.getArguments())).append(");");
            }
            _methodQueue.clear();
        }
        if (map != null) {
            sb.append("var map = openlayers._binds['").append(map.getUuid())//
                    .append("']; if (!map) map = openlayers._binds['").append(map.getUuid())//
                    .append("'] = {};")//创建地图对象
                    .append("map[_a.uuid] = _a;");
        }
        sb.append("return _a;})()");//立即调用匿名函数，返回 _a 对象。
        return sb.toString();
    }

    @Override
    public String toJSONString() {
        return toJSONString(null);
    }

    protected static class Method {
        private String _name;
        private Object[] _arguments;

        private Method(String name, Object[] arguments) {
            _name = name;
            _arguments = arguments;
        }

        public String getName() {
            return _name;
        }

        public Object[] getArguments() {
            return _arguments;
        }
    }
}
