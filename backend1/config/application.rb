# config/application.rb

require_relative "boot"

require "rails/all"

Bundler.require(*Rails.groups)

module FasTravel
  class Application < Rails::Application
    config.middleware.insert_before 0, Rack::Cors do
      allow do
        origins '*' 
        resource '*',
          headers: :any,
          methods: [:get, :post, :put, :patch, :delete, :options, :head]
      end
    end
  end
end