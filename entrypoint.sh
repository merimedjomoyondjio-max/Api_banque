#!/bin/sh
set -eu

raw_url="${SPRING_DATASOURCE_URL:-${DATABASE_URL:-}}"

if [ -n "$raw_url" ]; then
  case "$raw_url" in
    jdbc:*)
      export SPRING_DATASOURCE_URL="$raw_url"
      ;;
    postgresql://*|postgres://*)
      url_no_scheme="${raw_url#postgresql://}"
      url_no_scheme="${url_no_scheme#postgres://}"

      authority="${url_no_scheme%%/*}"
      path_and_query="${url_no_scheme#*/}"

      if [ "$authority" = "${authority#*@}" ]; then
        hostpart="$authority"
      else
        creds="${authority%@*}"
        hostpart="${authority#*@}"
        if [ -n "$creds" ]; then
          export SPRING_DATASOURCE_USERNAME="${creds%%:*}"
          if [ "$creds" != "${creds#*:}" ]; then
            export SPRING_DATASOURCE_PASSWORD="${creds#*:}"
          fi
        fi
      fi

      case "$hostpart" in
        *.*) ;;
        *)
          hostpart="${hostpart}.${RENDER_POSTGRES_HOST_SUFFIX:-oregon-postgres.render.com}"
          ;;
      esac

      export SPRING_DATASOURCE_URL="jdbc:postgresql://$hostpart/$path_and_query"
      ;;
    *)
      export SPRING_DATASOURCE_URL="jdbc:$raw_url"
      ;;
  esac
fi

exec java -Dserver.port="${PORT:-8080}" ${JAVA_TOOL_OPTIONS:-} -jar /app/app.jar
