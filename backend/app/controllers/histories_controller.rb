class HistoriesController < ApplicationController
  before_action :authorized

  # GET /histories
  def index
    histories = current_user.histories.order(travel_date: :desc)
    # Para coleções, usa render json direto ou each_serializer
    render json: histories, each_serializer: HistorySerializer
  end

  # POST /histories
  def create
    history = current_user.histories.new(history_params)
    
    if history.save
      # Para um único objeto, pode usar o serializer assim
      render json: HistorySerializer.new(history).serializable_hash, status: :created
    else
      render json: { errors: history.errors.full_messages }, status: :unprocessable_entity
    end
  end

  private

  def history_params
    params.require(:history).permit(:origin, :destiny, :travel_date)
  end
end