$progId = 1      #


$pair   = "user1:user1"
$bytes  = [System.Text.Encoding]::UTF8.GetBytes($pair)
$base64 = [Convert]::ToBase64String($bytes)
$headers = @{
    "Content-Type"  = "application/json"
    "Authorization" = "Basic $base64"
}

$body1 = @'
{
  "filmTitle": "Diabolique",
  "filmCast": "Simone Signoret",
  "filmGenres": "Thriller",
  "filmDurationMinutes": 105,
  "auditoriumName": "Hall 1",
  "startTime": "2025-03-03T20:00:00",
  "endTime":   "2025-03-03T22:00:00"
}
'@

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/programs/$progId/screenings" `
  -Method POST `
  -Headers $headers `
  -Body $body1



$body2 = @'
{
  "filmTitle": "Psycho",
  "filmCast": "Anthony Perkins",
  "filmGenres": "Horror",
  "filmDurationMinutes": 110,
  "auditoriumName": "Hall 2",
  "startTime": "2025-03-04T18:00:00",
  "endTime":   "2025-03-04T20:00:00"
}
'@

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/programs/$progId/screenings" `
  -Method POST `
  -Headers $headers `
  -Body $body2
