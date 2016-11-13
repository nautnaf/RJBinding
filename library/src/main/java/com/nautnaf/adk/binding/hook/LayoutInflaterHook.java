package com.nautnaf.adk.binding.hook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.nautnaf.adk.binding.parse.ViewAttributeParser;
import com.nautnaf.adk.binding.tool.ReflectionUtils;

public class LayoutInflaterHook {
  public LayoutInflaterHook() {
    throw new UnsupportedOperationException("Can not create an object.");
  }

  public static LayoutInflater hook(@NonNull Context ctx, @NonNull ViewAttributeParser parser) {
    LayoutInflater inflater = LayoutInflater.from(ctx);

    LayoutInflater.Factory factory = wrapFactory(inflater, parser);
    if (factory != null)
      ReflectionUtils.setField(LayoutInflater.class, "mFactory", inflater, factory);

    LayoutInflater.Factory2 factory2 = wrapFactory2(inflater, parser);
    if (factory2 != null) {
      ReflectionUtils.setField(LayoutInflater.class, "mFactory", inflater, factory2);
      ReflectionUtils.setField(LayoutInflater.class, "mFactory2", inflater, factory2);
    }

    LayoutInflater.Factory2 privateFactory2 = wrapPrivateFactory2(inflater, parser);
    ReflectionUtils.setField(LayoutInflater.class, "mPrivateFactory", inflater, privateFactory2);

    return inflater;
  }

  private static LayoutInflater.Factory wrapFactory(LayoutInflater inflater, ViewAttributeParser parser) {
    LayoutInflater.Factory factory = inflater.getFactory();
    if ((inflater.getFactory2() != null) || (factory == null)) {
      return null;
    } else {
      return new WrapFactory(factory, parser);
    }
  }

  private static LayoutInflater.Factory2 wrapFactory2(LayoutInflater inflater, ViewAttributeParser parser) {
    LayoutInflater.Factory2 factory2 = inflater.getFactory2();
    if (factory2 == null) {
      return null;
    } else {
      return new WrapFactory2(factory2, parser);
    }
  }

  private static LayoutInflater.Factory2 wrapPrivateFactory2(LayoutInflater inflater, ViewAttributeParser parser) {
    LayoutInflater.Factory2 privateFactory = ReflectionUtils.getField(LayoutInflater.class, "mPrivateFactory", inflater);
    LayoutInflater.Factory2 factory2 = (privateFactory != null) ? privateFactory : EMPTY_FACTORY2;
    return new WrapFactory2(new HookFactory2(inflater, factory2), parser);
  }

  private static final LayoutInflater.Factory EMPTY_FACTORY = new LayoutInflater.Factory() {
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
      return null;
    }
  };

  private static final LayoutInflater.Factory2 EMPTY_FACTORY2 = new LayoutInflater.Factory2() {
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
      return null;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
      return null;
    }
  };

  private static class WrapFactory implements LayoutInflater.Factory {
    final LayoutInflater.Factory _delegate;
    final ViewAttributeParser _parser;

    WrapFactory(LayoutInflater.Factory delegate, ViewAttributeParser parser) {
      _delegate = delegate;
      _parser = parser;
    }

    @Override
    public View onCreateView(String s, Context context, AttributeSet attrs) {
      View v = _delegate.onCreateView(s, context, attrs);
      if (v != null) _parser.parse(v, attrs);
      return v;
    }
  }

  private static class WrapFactory2 extends WrapFactory implements LayoutInflater.Factory2 {
    final LayoutInflater.Factory2 _delegate2;

    WrapFactory2(LayoutInflater.Factory2 delegate, ViewAttributeParser parser) {
      super(delegate, parser);
      _delegate2 = delegate;
    }

    @Override
    public View onCreateView(View view, String s, Context context, AttributeSet attrs) {
      View v = _delegate2.onCreateView(view, s, context, attrs);
      if (v != null) _parser.parse(v, attrs);
      return v;
    }
  }

  private static class HookFactory implements LayoutInflater.Factory {
    final LayoutInflater _inflater;
    final LayoutInflater.Factory _delegate;

    HookFactory(LayoutInflater inflater, LayoutInflater.Factory delegate) {
      _inflater = inflater;
      _delegate = delegate;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
      View v = _delegate.onCreateView(name, context, attrs);
      return createViewIfNull(v, name, attrs);
    }

    String fixViewPrefixName(@NonNull String viewName) {
      StringBuilder sb = new StringBuilder();
      if (viewName.equals("View") || viewName.equals("ViewGroup") || viewName.equals("ViewStub")) {
        sb.append("android.view.");
      } else if (viewName.equals("WebView")) {
        sb.append("android.webkit.");
      } else if (!viewName.contains(".")) {
        sb.append("android.widget.");
      }
      sb.append(viewName);
      return sb.toString();
    }

    private View createViewIfNull(View view, String name, AttributeSet attrs) {
      if (view != null) return view;
      String fullName = fixViewPrefixName(name);
      try {
        return _inflater.createView(fullName, null, attrs);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static class HookFactory2 extends HookFactory implements LayoutInflater.Factory2 {
    private final LayoutInflater.Factory2 _delegate2;
    private final Object[] _constructorArg;

    HookFactory2(LayoutInflater inflater, LayoutInflater.Factory2 delegate) {
      super(inflater, delegate);
      _delegate2 = delegate;
      _constructorArg = ReflectionUtils.getField(LayoutInflater.class, "mConstructorArgs", inflater);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
      View v = _delegate2.onCreateView(parent, name, context, attrs);
      return createViewIfNull(v, name, context, attrs);
    }

    private View createViewIfNull(View view, String name, Context context, AttributeSet attrs) {
      if (view != null) return view;
      String viewFullName = fixViewPrefixName(name);

      final Object lastContext = _constructorArg[0];
      _constructorArg[0] = context;
      try {
        return _inflater.createView(viewFullName, null, attrs);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      } finally {
        _constructorArg[0] = lastContext;
      }
    }
  }
}
