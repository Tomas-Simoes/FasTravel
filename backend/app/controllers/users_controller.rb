class UsersController < ApplicationController
  skip_before_action :authorized, only: [:create]

  def create 
      user = User.create!(user_params)
      @token = encode_token(user_id: user.id)

      render json: {
          user: UserSerializer.new(user), 
          token: @token
      }, status: :created
  rescue ActiveRecord::RecordInvalid => e 
    render json: { errors: e.record.errors.full_messages}, status: :unprocessable_entity
  end

  def show 
    user = User.find(params[:id])
    
    if current_user.id == user.id
      render json: UserSerializer.new(user)
    else
        render json: {message: 'Unauthorized'}, status: :forbidden
    end

    rescue ActiveRecord::RecordNotFound
      render json: {message: "User not found"}, status: :not_found
  end

  private

  def user_params 
      params.require(:user).permit(:email, :password)
  end
end