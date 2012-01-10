class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

        "/password/reset/$id"(controller: "user", action: "resetPassword")

		"500"(view:'/error500')
		"404"(view:'/error404')
	}
}
