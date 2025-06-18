import sys
import os
from openai import OpenAI

if len(sys.argv) != 2:
    print("Usage: python script.py <your_input>")
    sys.exit(1)

api_key = os.getenv("OPENAI_API_KEY")
if not api_key:
    print("OPENAI_API_KEY env variable is not configured")
    sys.exit(2)

user_input = sys.argv[1]
prompt_template = "Suppose you are a 5-year-old child. Answer the question as this child would: {}"
prompt = prompt_template.format(user_input)

client = OpenAI(api_key=api_key)
response = client.chat.completions.create(
    model="gpt-3.5-turbo", 
    messages=[
        {"role": "user", "content": prompt}
    ],
    max_tokens=100,
    temperature=0.8,
    n=1
)

print(response.choices[0].message.content.strip())