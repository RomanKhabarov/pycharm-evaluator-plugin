# PyCharm Evaluator Plugin


**PyCharm Evaluator Plugin** is a plugin for PyCharm that evaluates machine learning model outputs against a dataset and presents the results in a custom Tool Window UI.

- Input and Reference Output can be provided via `dataset.json` file in the root of your PyCharm project
- Runs multiple scripts in parallel
- Uses OpenAI to evaluate model outputs
- Displays results in a table



## Requirements

- PyCharm
- Environment variable `OPENAI_API_KEY` set with a valid OpenAI API key
- A `dataset.json` file located in the **root** of your project

Example dataset file:  
[dataset.json example](./src/main/resources/dataset.json)



## Usage


### Option 1: Using the built release

1. Go to the [GitHub Releases page](../../releases/latest)

2. Download the file: `pycharm-evaluator-plugin-1.0.0.zip`

3. In PyCharm:
    - Open **Settings / Preferences** → **Plugins**
    - Click the gear icon **⚙** → **Install Plugin from Disk...**
    - Select the downloaded `.zip` file
    - Restart PyCharm

4. Add a valid `dataset.json` file to your project root (you can copy [this one](./src/main/resources/dataset.json))

5. Make sure the environment variable `OPENAI_API_KEY` is set:
   ```bash
   export OPENAI_API_KEY=your_api_key_here

6. Open plugin window (View → Tool Windows → Evaluations) and click `Run`.


### Option 2: Run from source using `runIde`

1. Clone this repository:
   ```bash
   git clone https://github.com/RomanKhabarov/pycharm-evaluator-plugin.git
   cd pycharm-evaluator-plugin
   ```

2. Ensure you have set the environment variable `OPENAI_API_KEY`:
   ```bash
   export OPENAI_API_KEY=your_api_key_here
   ```

3. Open the project in IntelliJ IDEA

4. Run the plugin using the following Gradle command:
   ```
   ./gradlew runIde
   ```

5. A new PyCharm window will open with the plugin loaded. Open any project that contains a `dataset.json` file to use the plugin.


