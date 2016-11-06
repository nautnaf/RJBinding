package com.kesco.adk.binding.hook;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.kesco.adk.binding.tool.ReflectionUtils;

public class LayoutInflaterHook {
  public LayoutInflaterHook() {
    throw new UnsupportedOperationException("Can not create an object.");
  }

  public static void hook(Context ctx) {
    LayoutInflater inflater = LayoutInflater.from(ctx);

    LayoutInflater.Factory2 privateFactory2 = wrapPrivateFactory2(inflater);
    ReflectionUtils.setField(LayoutInflater.class, "mPrivateFactory", inflater, privateFactory2);
  }

  private static LayoutInflater.Factory2 wrapPrivateFactory2(LayoutInflater inflater) {
    LayoutInflater.Factory2 privateFactory = ReflectionUtils.getField(LayoutInflater.class, "mPrivateFactory", inflater);
    LayoutInflater.Factory2 factory2 = (privateFactory != null) ? privateFactory : EMPTY_FACTORY2;
    return new HookFactory2(inflater, factory2);
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

  static class HookFactory implements LayoutInflater.Factory {
    final LayoutInflater inflater;
    final LayoutInflater.Factory delegate;

    HookFactory(LayoutInflater inflater, LayoutInflater.Factory delegate) {
      this.inflater = inflater;
      this.delegate = delegate;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
      View v = delegate.onCreateView(name, context, attrs);
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
        return inflater.createView(fullName, null, attrs);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  static class HookFactory2 extends HookFactory implements LayoutInflater.Factory2 {
    private final LayoutInflater.Factory2 delegate2;
    private final Object[] constructorArg;

    HookFactory2(LayoutInflater inflater, LayoutInflater.Factory2 delegate) {
      super(inflater, delegate);
      delegate2 = delegate;
      constructorArg = ReflectionUtils.getField(LayoutInflater.class, "mConstructorArgs", inflater);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
      View v = delegate2.onCreateView(parent, name, context, attrs);
      return createViewIfNull(v, name, context, attrs);
    }

    private View createViewIfNull(View view, String name, Context context, AttributeSet attrs) {
      if (view != null) return view;
      String viewFullName = fixViewPrefixName(name);

      final Object lastContext = constructorArg[0];
      constructorArg[0] = context;
      try {
        return inflater.createView(viewFullName, null, attrs);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      } finally {
        constructorArg[0] = lastContext;
      }
    }
  }
}
