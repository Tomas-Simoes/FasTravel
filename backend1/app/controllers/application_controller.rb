
class ApplicationController < ActionController::API

  def encode_token(payload)
      JWT.encode(payload, Rails.application.secret_key_base) 
  end

  def decoded_token
      header = request.headers['Authorization']
      if header
          token = header.split(" ")[1]
          begin
              JWT.decode(token, Rails.application.secret_key_base)
          rescue JWT::DecodeError
              nil
          end
      end
  end
end