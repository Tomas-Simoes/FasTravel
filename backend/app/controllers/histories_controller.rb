class HistoriesController < ApplicationController
  before_action :authorized

  # GET /histories
  def index
    histories = current_user.histories.order(travel_date: :desc)
    render json: HistorySerializer.new(histories) # se usar um Serializer
  end

  # POST /histories
  # Cria um novo registo de histórico para o utilizador autenticado
  def create
    history = current_user.histories.new(history_params)
    
    if history.save
      render json: history, status: :created
      # OU
      # render json: HistorySerializer.new(history) # se usar um Serializer
    else
      render json: { errors: history.errors.full_messages }, status: :unprocessable_entity
    end
  end

  private

  def history_params
    # Certifique-se de permitir os campos que definiu no modelo History
    params.require(:history).permit(:origin, :destiny, :travel_date)
  end
end