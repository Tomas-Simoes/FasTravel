# config/routes.rb
# frozen_string_literal: true

Rails.application.routes.draw do
    # POST login  -> AuthController#login
    post 'login', to: 'auth#login'

    # GET /protected -> V1::AuthController#protected
    get 'protected', to: 'auth#protected' 

    # POST /users    -> UsersController#create 
    # GET /users/:id -> UsersController#show
    resources :users, only: [:create, :show]
end