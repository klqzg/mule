/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.execution;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.module.extension.api.util.MuleExtensionUtils.getInitialiserEvent;
import static org.mule.runtime.module.extension.internal.util.MuleExtensionUtils.isNonBlocking;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.meta.model.ComponentModel;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.privileged.event.BaseEventContext;
import org.mule.runtime.extension.api.runtime.operation.CompletableComponentExecutor;
import org.mule.runtime.extension.api.runtime.operation.CompletableComponentExecutorFactory;
import org.mule.runtime.module.extension.internal.runtime.objectbuilder.DefaultObjectBuilder;
import org.mule.runtime.module.extension.internal.runtime.resolver.StaticValueResolver;
import org.mule.runtime.module.extension.internal.runtime.resolver.ValueResolvingContext;
import org.mule.runtime.module.extension.internal.util.ReflectionCache;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * An implementation of {@link CompletableComponentExecutorFactory} which produces instances of
 * {@link ReflectiveMethodOperationExecutor}.
 *
 * @param <T> the type of the class in which the implementing method is declared
 * @since 3.7
 */
public final class ReflectiveOperationExecutorFactory<T, M extends ComponentModel> implements
    CompletableComponentExecutorFactory<M> {

  private final Class<T> implementationClass;
  private final Method operationMethod;

  public ReflectiveOperationExecutorFactory(Class<T> implementationClass, Method operationMethod) {
    checkArgument(implementationClass != null, "implementationClass cannot be null");
    checkArgument(operationMethod != null, "operationMethod cannot be null");

    this.implementationClass = implementationClass;
    this.operationMethod = operationMethod;
  }

  @Override
  public CompletableComponentExecutor<M> createExecutor(M operationModel, Map<String, Object> parameters) {
    DefaultObjectBuilder objectBuilder = new DefaultObjectBuilder(implementationClass, new ReflectionCache());
    parameters.forEach((k, v) -> objectBuilder.addPropertyResolver(k, new StaticValueResolver<>(v)));
    Object delegate;
    CoreEvent initialiserEvent = null;
    try {
      initialiserEvent = getInitialiserEvent();
      delegate = objectBuilder.build(ValueResolvingContext.builder(initialiserEvent).build());
    } catch (Exception e) {
      throw new MuleRuntimeException(createStaticMessage("Could not create instance of operation class "
          + implementationClass.getName()), e);
    } finally {
      if (initialiserEvent != null) {
        ((BaseEventContext) initialiserEvent.getContext()).success();
      }
    }

    if (isNonBlocking(operationModel)) {
      return new NonBlockingReflectiveMethodOperationExecutor<>(operationModel, operationMethod, delegate);
    }

    return new ReflectiveMethodOperationExecutor<>(operationModel, operationMethod, delegate);
  }
}
