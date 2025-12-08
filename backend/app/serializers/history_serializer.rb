class HistorySerializer < ActiveModel::Serializer
  attributes :id, :origin, :destiny, :travel_date, :created_at 
end
