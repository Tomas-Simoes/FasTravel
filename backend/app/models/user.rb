# frozen_string_literal: true

# teste
class User < ApplicationRecord
  has_secure_password
  validates :email, presence: true, uniqueness: {case_sensitive: false}, format: {with: URI::MailTo::EMAIL_REGEXP}
  validates :password, length: {minimum: 8}, if: :password_required? 
  has_many :histories

  before_save :downcase_email

  private 

  def downcase_email
    self.email = email.downcase
  end
  
  def password_required?
    new_record? || password.present?
  end
end
