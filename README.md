# Go AI Tests for Goland IDE (WIP)

GO AI Tests is an IntelliJ IDEA plugin that helps generate Golang tests for your code using AI.
It can automatically generate a tests for the current function or selected block.

## Getting Started

To use Go AI Tests, you'll need to install it as a plugin in your IntelliJ editor (Goland). Here are the steps:

1. Go to `Settings` -> `Plugins` -> `Marketplace` in your IntelliJ editor.
2. Search for "Go AI Tests" and click on the `Install` button.
3. Once the plugin is installed, restart IntelliJ IDEA editor to activate it.
4. Retrieve your ChatGPT API key from [OpenAI](https://platform.openai.com/account/api-keys).
5. Set the API Key in `Settings` -> `Tools` -> `AI Tests`

## Using Go AI Tests

AI tests provides two main features:

✅ Generate Golang tests for current or selected function.  

To generate an AI Tests, simply place your cursor inside a function or select a block for which you want to generate a test for, or select a block of code, then Go to `Code` -> `Generate` or Press `Ctrl + N` (or `Cmd + N` on a Mac) and select `Generate Ai Tests` from the menu.  
The plugin will then use AI to generate a test function for your function or your block of code.

## Contributing

If you'd like to contribute to Go AI Tests, please feel free to submit a pull request. We welcome contributions of all types, from bug fixes to new features.

## License

Go AI Tests is licensed under the MIT License. See the `LICENSE` file for more information.

## Contact

If you have any questions or feedback about Go AI Tests, please feel free to contact us at y.alhyane@gmail.com
