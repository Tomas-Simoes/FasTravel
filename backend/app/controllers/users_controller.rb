class UsersController < ApplicationController
  skip_before_action :authorized, only: [:create]

  def create 
    user = User.new(user_params)
    if user.save
      token = encode_token(user_id: user.id)
      render json: { user: UserSerializer.new(user), token: token }, status: :created
    else
      render json: { errors: user.errors.full_messages }, status: :unprocessable_entity
    end
  rescue ActionController::ParameterMissing => e
    render json: { errors: ["Param missing: #{e.param}"] }, status: :bad_request
  end

  private

  def user_params 
    params.require(:user).permit(:email, :password)
  end
end
