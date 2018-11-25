package io.rong.eventbus.util;

public abstract interface HasExecutionScope
{
  public abstract Object getExecutionScope();
  
  public abstract void setExecutionScope(Object paramObject);
}
