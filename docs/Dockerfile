# Create an appropriate container for running Jekyll similar to how it would run on Github Pages.

# Use Ruby 2.7.4 since this is the current version of Ruby used by GitHub Pages, see here: https://pages.github.com/versions/
FROM ruby:2.7.4-alpine3.14

# Add all things that Jekyll depends upons: `gcc` and `make`
# These are all listed here: https://jekyllrb.com/docs/installation/#requirements
RUN apk update
RUN apk add --no-cache build-base gcc make

# Now create a Ruby environment similar to what's available in Github Pages, by installing all the Ruby gems that will be available in Github Pages.
#
# Thankfully, this is easy to do because Github manages a Ruby gem, 'github-pages', that aggregates all the related dependencies.
#   See : https://github.com/github/pages-gem
#
# So by installing this single Ruby gem we'll get all the gems we need, including Jekyll.
RUN bundle init
#   Sadly, when version 1.17 of 'ffi' (which is a transitive dependency of the 'github-pages' gem) was released it dropped support for Ruby 2.7.4,
#   and hence it is not backwards compatible with version 1.16 of 'ffi': i.e. this change broke semantic versioning rules.
#   This fact requires that we forcefully depend upon a version of 'ffi' less than '1.17' in order to pull in a version of 'ffi' that works with 'github-pages'.
RUN bundle add ffi --version "<1.17"
RUN bundle add github-pages --group=":jekyll-plugins"
