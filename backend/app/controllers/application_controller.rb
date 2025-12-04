class ApplicationController < ActionController::API
    before_action :authorized

    JWT_SECRET = Rails.application.credentials.dig(:jwt_secret_key) || Rails.application.secret_key_base
    JWT_ALGORITHM = 'HS256'
    TOKEN_EXPIRATION = 24.hours
    
    def encode_token(payload)
      payload[:exp] = TOKEN_EXPIRATION.from_now.to_i
      JWT.encode(payload, JWT_SECRET, JWT_ALGORITHM)
    end
  
    def decoded_token
      header = request.headers['Authorization']
      return nil unless header
      
      token = header.split(" ")[1]
      return nil unless token 

      begin
        JWT.decode(token, JWT_SECRET, true, algorithm: JWT_ALGORITHM)      rescue JWT::DecodeError

      rescue JWT::ExpiredSignature
        nil 
      rescue JWT::DecodeError 
        nil 
      end
    end
  
    def current_user
      return @current_user if defined?(@current_user)

      if decoded_token
        user_id = decoded_token[0]['user_id']
        @current_user = User.find_by(id: user_id)
      else 
        @current_user = nil
      end
    end
  
    def authorized
      unless current_user
        render json: { message: 'Please log in' }, status: :unauthorized
      end
    end
  end
  