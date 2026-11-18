
import json
import os
import re
import time
import urllib.parse
import urllib.request
import urllib.error
from getpass import getpass

POSTERS = {
    "Inception": "inception.jpg",
    "The Dark Knight": "the-dark-knight.jpg",
    "Interstellar": "interstellar.jpg",
    "3 Idiots": "3-idiots.jpg",
    "Dangal": "dangal.jpg",
    "Spider-Man: No Way Home": "spider-man-no-way-home.jpg",
    "The Shawshank Redemption": "the-shawshank-redemption.jpg",
    "The Godfather": "the-godfather.jpg",
    "Titanic": "titanic.jpg",
    "Avatar": "avatar.jpg",
    "Avengers: Endgame": "avengers-endgame.jpg",
    "Jurassic Park": "jurassic-park.jpg",
    "Forrest Gump": "forrest-gump.jpg",
    "Sholay": "sholay.jpg",
    "Dilwale Dulhania Le Jayenge": "dilwale-dulhania-le-jayenge.jpg",
    "Oppenheimer": "oppenheimer.jpg",
    "Dune: Part Two": "dune-part-two.jpg",
    "Jawan": "jawan.jpg",
    "RRR": "rrr.jpg",
    "Barbie": "barbie.jpg"
}

def normalize(title):
    return re.sub(r"[^a-z0-9]", "", title.lower())

def get_json(url):
    request = urllib.request.Request(
        url,
        headers={"User-Agent": "TheatrelyPosterDownloader/1.0"}
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.loads(response.read().decode("utf-8"))

def main():
    print("THEATRELY - MOVIE POSTER DOWNLOADER")
    print("-----------------------------------")

    api_key = getpass("Paste your TMDB API KEY (input stays hidden): ").strip()

    if not api_key:
        print("No API key entered. Exiting.")
        return

    poster_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "posters")
    os.makedirs(poster_dir, exist_ok=True)

    successful = []
    failed = []

    for movie, filename in POSTERS.items():
        destination = os.path.join(poster_dir, filename)

        if os.path.exists(destination) and os.path.getsize(destination) > 0:
            print(f"[SKIP] {movie} - poster already exists")
            successful.append(movie)
            continue

        try:
            params = urllib.parse.urlencode({
                "api_key": api_key,
                "query": movie,
                "include_adult": "false",
                "language": "en-US"
            })

            search_url = (
                "https://api.themoviedb.org/3/search/movie?" + params
            )
            data = get_json(search_url)
            results = data.get("results", [])

            if not results:
                raise Exception("Movie not found on TMDB")

            exact_matches = [
                item for item in results
                if normalize(item.get("title", "")) == normalize(movie)
                or normalize(item.get("original_title", "")) == normalize(movie)
            ]

            selected = exact_matches[0] if exact_matches else results[0]
            poster_path = selected.get("poster_path")

            if not poster_path:
                raise Exception("No poster image available")

            image_url = "https://image.tmdb.org/t/p/w500" + poster_path

            request = urllib.request.Request(
                image_url,
                headers={"User-Agent": "TheatrelyPosterDownloader/1.0"}
            )

            with urllib.request.urlopen(request, timeout=30) as response:
                image_data = response.read()

            if not image_data:
                raise Exception("Downloaded image was empty")

            with open(destination, "wb") as file:
                file.write(image_data)

            print(f"[OK] {movie} -> {filename}")
            successful.append(movie)

        except Exception as error:
            print(f"[FAILED] {movie}: {error}")
            failed.append(movie)

        time.sleep(0.3)

    print("\n-----------------------------------")
    print(f"Downloaded/already present: {len(successful)}/20")
    print(f"Failed: {len(failed)}")

    if failed:
        print("\nMovies needing attention:")
        for movie in failed:
            print("-", movie)

    print("\nPoster folder:", poster_dir)
    print("Done.")

if __name__ == "__main__":
    main()
