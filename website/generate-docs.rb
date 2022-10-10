#!/usr/bin/env ruby

require 'diffy'
require 'fileutils'
require 'json'
require 'mustache'

# XXX: Perhaps split this out into multiple Ruby files for readability.
# This script assumes to be ran inside `$repo_root/website`.
repo_root = ".."
generated_json_files_path = "#{repo_root}/error-prone-contrib/target/docs"
bug_pattern_path = "bugpatterns/"
refaster_rules_path = "refasterrules/"

def retrieve_patterns(path)
  Dir.glob("#{path}/*.json").inject({ "bug_patterns" => {}, "refaster_rules" => {} }) { |memo, file_name|
    if (match = /.+(?<type>bug-pattern|refaster).*-(?<name>\w+?)(Test(Input|Output)?)?\.json/.match file_name)
      type, name = match[:type] == 'bug-pattern' ? 'bug_patterns' : 'refaster_rules', match[:name]
      memo[type][name] ||= []
      memo[type][name] << file_name
      memo
    else
      p "Could not determine type or name of file #{file_name}"
      exit 1
    end
  }
end

def parse_json(path)
  File.open(path) do |file|
    JSON.load(file)
  end
end

def get_bug_pattern_file_type(file)
  file.end_with?('Test.json') ? "test_file" : "bug_pattern"
end

def get_refaster_file_type(file)
  if file.end_with?('TestInput.json')
    'test_input'
  elsif file.end_with?('TestOutput.json')
    'test_output'
  else
    'refaster_rule'
  end
end

def get_severity_color(severity)
  case severity
  when 'ERROR'
    "red"
  when 'WARNING'
    "yellow"
  when 'SUGGESTION'
    "green"
  else
    "blue"
  end
end

puts 'Generating homepage...'
homepage = File.read("#{repo_root}/README.md").gsub("src=\"website/", "src=\"").gsub("srcset=\"website/", "srcset=\"")
File.write("index.md", Mustache.render(File.read("index.mustache"), homepage))

if !system('type mvn > /dev/null')
  puts '[WARN] Could not find `mvn` on PATH. Skipping data extraction from source code of bug patterns and Refaster (test) classes.'
else
  puts 'Generating JSON data for bug patterns and Refaster rules...'
  # XXX: Remove `-Dvalidation.skip`.
  system("mvn -f #{repo_root}/pom.xml -T1C clean install -DskipTests -Dverification.skip -Pdocgen")
end
# XXX: Rename variable, it is confusing. It is a collection of all file paths for Bug Patterns and Refaster Rules.
patterns = retrieve_patterns(generated_json_files_path)
FileUtils.mkdir_p(bug_pattern_path)
FileUtils.mkdir_p(refaster_rules_path)

puts 'Generating bug patterns pages...'
patterns['bug_patterns'].values.each { |files|
  data = files.map { |file| parse_json(file) }.inject(:merge)

  # XXX: Introduce a new `@DocumentationExample` annotation and derive samples from that.
  # XXX: While awaiting the previous XXX, make a temporary decision on which and how many samples to show.
  diff_samples = data['replacementTests']&.map { |testCase| Diffy::Diff.new(testCase['inputLines'], testCase['outputLines']).to_s.rstrip } || []
  java_samples = data['identificationTests']&.map { |testCase| testCase.rstrip } || []

  # XXX: Add suppression data (once available).
  render = Mustache.render(File.read("bug-pattern.mustache"), {
    diff_samples: diff_samples,
    explanation: data['explanation'],
    has_samples: java_samples.length > 0 || diff_samples.length > 0,
    java_samples: java_samples,
    location: "https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/#{data['fullyQualifiedName'].gsub(/\./, "/")}.java",
    name: data['name'],
    severity_level: {
      content: data['severityLevel'],
      color: get_severity_color(data['severityLevel'])
    },
    summary: data['summary'],
    tags: data['tags']
  })
  File.write("#{bug_pattern_path}/#{data['name']}.md", render)
}

puts 'Generating Refaster rules pages...'
patterns['refaster_rules'].values.each { |files|
  collection_files = files.to_h { |file| [get_refaster_file_type(file), parse_json(file)] }
  # XXX: Once we have rule non-test data, extract collection from there.
  collection_name = collection_files['test_input']['templateCollection']

  rules = []
  # XXX: Once we have rule non-test data, iterate over that instead and extract input like we do output now.
  collection_files['test_input']["templateTests"].each { |rule|
    rule_name = rule['templateName']
    # We need to strip newlines and add them to the end as a workaround to https://github.com/samg/diffy/issues/88.
    input = "#{rule['templateTestContent'].strip}\n"
    output = "#{collection_files['test_output']['templateTests'].find { |testCase| testCase['templateName'] == rule_name }['templateTestContent'].strip}\n"
    rules << {
      collection_name: collection_name,
      # XXX: Derive from FQN passed when extracting RefasterRuleData, ideally with anchor info (source code line) to the inner class.
      location: "https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/#{collection_name}.java",
      rule_name: rule_name,
      samples: [Diffy::Diff.new(input, output).to_s.rstrip],
      # XXX: Extract suggestion and tags instead of hardcoding it.
      severity_level: {
        content: 'SUGGESTION',
        color: get_severity_color('SUGGESTION')
      },
      tags: ['Simplification']
      # XXX: Extract summary, suppression information.
    }
  }

  render = Mustache.render(File.read("refaster-rule-collection.mustache"), {
    collection_name: collection_name,
    # XXX: Derive from FQN passed when extracting RefasterRuleData.
    location: "https://github.com/PicnicSupermarket/error-prone-support/blob/master/error-prone-contrib/src/main/java/tech/picnic/errorprone/refasterrules/#{collection_name}.java",
    rules: rules,
    severity_level: {
      content: 'SUGGESTION',
      color: get_severity_color('SUGGESTION')
    },
    tags: ['Simplification'] })
  File.write("#{refaster_rules_path}/#{collection_name}.md", render)
}

puts 'Generating website using Jekyll...'
system("bundle exec jekyll build")
