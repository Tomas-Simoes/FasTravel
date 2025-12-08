class CreateHistories < ActiveRecord::Migration[7.1]
  def change
    create_table :histories do |t|
      t.string :origin
      t.string :destiny
      t.date :travel_date
      t.references :user, foreign_key: true

      t.timestamps
    end
  end
end
