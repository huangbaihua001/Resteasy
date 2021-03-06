package org.jboss.resteasy.security.doseta;

import org.jboss.resteasy.annotations.security.doseta.Signed;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.BindingPriority;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Configurable;
import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@ConstrainedTo(ConstrainedTo.Type.SERVER)
public class ServerDigitalSigningHeaderDecoratorFeature implements DynamicFeature
{
   @Override
   public void configure(ResourceInfo resourceInfo, Configurable configurable)
   {
      Signed signed = resourceInfo.getResourceMethod().getAnnotation(Signed.class);
      if (signed == null)
      {
         signed = (Signed) resourceInfo.getResourceClass().getAnnotation(Signed.class);
      }
      if (signed == null) return;

      configurable.register(new DigitalSigningHeaderDecorator(signed));
   }

   /**
    * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
    * @version $Revision: 1 $
    */
   @BindingPriority(BindingPriority.HEADER_DECORATOR)
   public static class DigitalSigningHeaderDecorator extends AbstractDigitalSigningHeaderDecorator implements ContainerResponseFilter
   {
      public DigitalSigningHeaderDecorator(Signed signed)
      {
         this.signed = signed;
      }

      @Override
      public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException
      {
         KeyRepository repository = ResteasyProviderFactory.getContextData(KeyRepository.class);
         DKIMSignature header = createHeader(repository);
         responseContext.getHeaders().add(DKIMSignature.DKIM_SIGNATURE, header);
      }
   }
}
