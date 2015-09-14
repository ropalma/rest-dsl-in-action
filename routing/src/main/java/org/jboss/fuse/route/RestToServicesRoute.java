package org.jboss.fuse.route;

import org.jboss.fuse.model.Blog;
import org.jboss.fuse.service.ElasticSearchService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestToServicesRoute extends RouteBuilder {

    final static Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    @Override
    public void configure() throws Exception {

        restConfiguration().component("jetty").host("0.0.0.0").port("9191")
                .enableCORS(true)
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true");

        rest("/blog/article").id("rest-blog-service").produces("application/json").consumes("application/json")

           .get("/search/id/{id}").id("rest-searchbyid")
               .to("direct:searchById")

           .get("/search/user/{user}").id("rest-searchbyuser").outTypeList(Blog.class)
              .to("direct:searchByUser")

           .put("/{id}").id("rest-put-article").type(Blog.class)
               .to("direct:add")

           .delete("/{id}").id("rest-deletearticle").type(Blog.class)
              .to("direct:remove");

        /* 
         * Workaround to support HTTP OPTIONS request required for Swagger API when CORS is enabled
         * The Allow Header reports the operations supported.
         * A new verb has been added - https://issues.apache.org/jira/browse/CAMEL-9129 but it will be available for 2.16 or 2.15.x
         */
        rest("/blog/article/")
           .verb("options")
           .route()
           .setHeader("Access-Control-Allow-Origin", constant("*"))
           .setHeader("Access-Control-Allow-Methods", constant("GET, HEAD, POST, PUT, DELETE, OPTIONS"))
           .setHeader("Access-Control-Allow-Headers", constant("Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers"))
           .setHeader("Allow", constant("GET, HEAD, POST, PUT, DELETE, OPTIONS"));

    }
}
