# This Makefile is nothing more than a shortcuts/macros for useful commands.
# So all targets are 'PHONY', as none of the targets are the name of a file on disk.
.PHONY: dev-server-create-docker-image dev-server-start draft-doom_wad_parser draft-tic_tac_toe publish-all-scala-js-files prepare-to-commit encode-doom-binary-resources

# Create the docker image that'll be used by dev-server-start
dev-server-create-docker-image:
	docker buildx build -t to-run-jekyll ./docs

# Start up an instance of Jekyll at 0.0.0.0:4000, with a root directory of /docs, and enable live reloading 
dev-server-start:
	docker run --rm -it -v ./docs:/home/mypage -w /home/mypage -p 4000:4000 -p 8000:8000 to-run-jekyll bundle exec jekyll serve --host 0.0.0.0 --port 4000 --livereload --livereload_port 8000

# Draft a dev version of the doom_wad_parser project to the directory where it's used by the website
# TODO: add to fastLinkJSAndCopy the ability to pass in a target directory arg, so fastLinkJSAndCopy doesn't have to hardcode an outside target desitination
draft-doom_wad_parser:
	cd doom_wad_parser && sbt fastLinkJSAndCopy

# Draft a dev version of the tic_tac_toe project to the directory where it's used by the website
# TODO: add to fastLinkJSAndCopy the ability to pass in a target directory arg, so fastLinkJSAndCopy doesn't have to hardcode an outside target desitination
draft-tic_tac_toe:
	cd tic_tac_toe && sbt fastLinkJSAndCopy

# Produce and copy production-level versions of all Scala.js projects
publish-all-scala-js-files:
	cd doom_wad_parser && sbt fullLinkJSAndCopy
	cd tic_tac_toe && sbt fullLinkJSAndCopy

DOOM_BINARY_RESOURCES_FOLDER = docs/_includes/doom
DOOM_BINARY_RESOURCES = $(addprefix $(DOOM_BINARY_RESOURCES_FOLDER)/, doom.wasm DOOM1.WAD freedoom1.wad freedoom2.wad)
DOOM_BINARY_RESOURCES_AS_BASE64 = $(addsuffix .base64, $(DOOM_BINARY_RESOURCES))

# Produce any and all "X.base64" resources by encoding the "X" resource via base64
%.base64: %
	@echo [Encoding $< via Base64]
	openssl base64 -in $< -out $@

encode-doom-binary-resources: $(DOOM_BINARY_RESOURCES_AS_BASE64)

# TODO: leverage Github actions so all generated files can be built from source as needed by Github.
# Once that's true then there'll be no need for this make target, which does extra work to 'prepare' this
# repository so that all generated files that are in source control are properly updated.
prepare-to-commit: publish-scala-js-files encode-doom-binary-resources
