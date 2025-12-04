class AuthController < ApplicationController
  skip_before_action :authorized, only: [:login]
  before_action :check_login_attempts, only: [:login]

  def login
    user = User.find_by!(email: login_params[:email]&.downcase)

    if user&.authenticate(login_params[:password])
      reset_login_attempts
      token = encode_token(user_id: user.id)
      
      render json: {
        user: UserSerializer.new(user),
        token: token
      }, status: :ok
    else
      increment_login_attempts
      render json: {message: 'Invalid credentials'}, status: :unauthorized
    end
  end

  def protected
    render json: current_user, serializer: UserSerializer
  end

  private 
  def login_params
    params.require(:auth).permit(:email, :password)
  end

  def check_login_attempts
    key = "login_attempts_#{request.remote_ip}"
    attempts = Rails.cache.read(key) || 0 

    if attempts >= 5
      render json: {
        message: "Too many attempts. Please try again in 15 minutes."
      }, status: :too_many_requests
    end
  end

  def increment_login_attempts
    key = "login_attempts_#{request.remote_ip}"
    attempts = Rails.cache.read(key) || 0
    Rails.cache.write(key, attempts + 1, expires_in: 15.minutes)
  end

  def reset_login_attempts
    Rails.cache.delete("login_attempts_#{request.remote_ip}")
  end
end
