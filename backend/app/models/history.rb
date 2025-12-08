class History < ApplicationRecord
  validates :origin, presence: true
  validates :destiny, presence: true
  
  belongs_to :user
end
